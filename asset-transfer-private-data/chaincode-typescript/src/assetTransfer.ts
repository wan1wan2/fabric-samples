import {Context, Contract, Info, Returns, Transaction} from 'fabric-contract-api';
import {Asset, AssetDetail} from './models/asset';
import { BaseModel } from './models/BaseModel';
import { TransferAgreement } from './models/transferAgreement';

const assetCollection = "assetCollection"
const transferAgreementObjectType = "transferAgreement"

@Info({title: 'AssetTransfer', description: 'Smart contract for trading assets'})
export class AssetTransferContract extends Contract {

    @Transaction(false)
    public TestConnection(): string {
        return 'connection success';
    }

    @Transaction()
    public async CreateAsset(ctx: Context): Promise<void> {
        const transientMap: Map<string, Uint8Array> = ctx.stub.getTransient();
        const transientAssetJson = transientMap.get('asset_properties');
        if (!transientAssetJson) {
            throw new Error(`asset not found in the transient map input`);
        }
        const transientAsset: Asset = JSON.parse(transientAssetJson.toString());
        if (!transientAsset) {
            throw new Error(`fail to parse asset properties json: ${transientAssetJson.toString()}`);
        }

        // Check if asset already exists
        let assetBytes = await ctx.stub.getPrivateData(assetCollection, transientAsset.ID);
        if (assetBytes && assetBytes.length > 0) {
            console.log(`Asset already exists: ${transientAsset.ID}`);
            throw new Error(`this asset already exists: ${transientAsset.ID}, ${assetBytes.toString()}`);
        }

        // Get ID of submitting client identity
        let clientId = this.submittingClientIdentity(ctx);
        if (!clientId || clientId.length === 0) {
            throw new Error(`get client Id error`);
        }

        this.verifyClientOrgMatchesPeerOrg(ctx);
        const assetInput = new Asset(transientAsset);
        assetInput.Owner = clientId;

        try {
            await ctx.stub.putPrivateData(assetCollection, assetInput.ID, assetInput.serialize());
        } catch (e) {
            throw new Error(`CreateAsset Error ${e}`);
        }

        try {
            const assetDetail: AssetDetail = new AssetDetail({
                ID: assetInput.ID,
                AppraisedValue: transientAsset.AppraisedValue
            });
            const collectName = this.getCollectionName(ctx);
            await ctx.stub.putPrivateData(collectName, assetDetail.ID, assetDetail.serialize());
        } catch (e) {
            throw new Error(`CreateAssetDetail Error ${e}`);
        }
        
    }

    @Transaction()
    public async AgreeTransfer(ctx: Context): Promise<void> {
        // Get ID of submitting client identity
        let clientId = this.submittingClientIdentity(ctx);
        if (!clientId || clientId.length === 0) {
            throw new Error(`get client Id error`);
        }

        const transientMap: Map<string, Uint8Array> = ctx.stub.getTransient();
        const transientAssetJson = transientMap.get('asset_value');
        if (!transientAssetJson) {
            throw new Error(`asset not found in the transient map input`);
        }
        const transientAsset: Asset = JSON.parse(transientAssetJson.toString());
        if (!transientAsset) {
            throw new Error(`fail to parse asset properties json: ${transientAssetJson.toString()}`);
        }

        const assetStr = await this.ReadAsset(ctx, transientAsset.ID);
        if (!assetStr || assetStr.length === 0) {
            throw new Error(`error reading asset: ${transientAsset.ID}`);
        }
        this.verifyClientOrgMatchesPeerOrg(ctx);
        
        try {
            const assetDetail: AssetDetail = new AssetDetail({
                ID: transientAsset.ID,
                AppraisedValue: transientAsset.AppraisedValue
            });
            const orgCollectionName = this.getCollectionName(ctx);
            await ctx.stub.putPrivateData(orgCollectionName, assetDetail.ID, assetDetail.serialize());
        } catch (e) {
            throw new Error(`CreateAssetDetail Error ${e}, asset id: ${transientAsset.ID}`);
        }

        // create agreement record
        let transferAgreeKey = ctx.stub.createCompositeKey(transferAgreementObjectType, [transientAsset.ID]);
        try {
            await ctx.stub.putPrivateData(assetCollection, transferAgreeKey, Buffer.from(clientId));
        } catch (e) {
            throw new Error(`Create Transfer Agree Error ${e}, asset id: ${transientAsset.ID}`);
        }

    }

    // input asset_owner
    // assetID: string
    // buyerMSP: string
    @Transaction()
    public async TransferAsset(ctx: Context): Promise<void> {

        const transientMap: Map<string, Uint8Array> = ctx.stub.getTransient();
        const transientJson = transientMap.get('asset_owner');
        if (!transientJson || transientJson.length === 0) {
            throw new Error(`asset owner not found in the transient map`);
        }

        interface TransientTransferInput {
            assetID: string,
            buyerMSP: string
        }

        let transientInput: TransientTransferInput = JSON.parse(transientJson.toString());
        if (!transientInput) {
            throw new Error(`fail to parse ${transientJson.toString()}`);
        } else if (!transientInput.assetID || transientInput.assetID.length === 0) {
            throw new Error(`input assetID empty`);
        } else if (!transientInput.buyerMSP || transientInput.buyerMSP.length === 0) {
            throw new Error(`input buyerMSP empty`);
        }

        this.verifyClientOrgMatchesPeerOrg(ctx);

        let assetObj = await this.ReadAsset(ctx, transientInput.assetID);
        let asset: Asset = new Asset(JSON.parse(assetObj));

        await this.verifyAgreement(ctx, transientInput.assetID, asset.Owner, transientInput.buyerMSP);

        let transferAgreementObj = await this.ReadTransferAgreement(ctx, transientInput.assetID);
        if (transferAgreementObj == null) {
            throw new Error(`failed ReadTransferAgreement to find buyerID: ${transientInput.assetID}`);
        }
        let transferAgreement: TransferAgreement = JSON.parse(transferAgreementObj);
        if (!transferAgreement || !transferAgreement.BuyerID || transferAgreement.BuyerID.length === 0) {
            throw new Error(`BuyerID not found in TransferAgreement for ${transientInput.assetID}`);
        }

        asset.Owner = transferAgreement.BuyerID;
        await ctx.stub.putPrivateData(assetCollection, asset.ID, asset.serialize());

        const ownersCollection = this.getCollectionName(ctx);

        // Delete the asset appraised value from this organization's private data collection
        await ctx.stub.deletePrivateData(ownersCollection, transientInput.assetID);

        // Delete the transfer agreement from the asset collection
        const transferAgreeKey = ctx.stub.createCompositeKey(transferAgreementObjectType, [transientInput.assetID]);
        await ctx.stub.deletePrivateData(assetCollection, transferAgreeKey);
    }

    @Transaction(false)
    @Returns("string")
    public async ReadAsset(ctx: Context, assetId: string): Promise<string> {
        let assetBuffer = await ctx.stub.getPrivateData(assetCollection, assetId);
        if (!assetBuffer || assetBuffer.length === 0) {
            console.error(`fail to load asset: ${assetId}`);
            throw new Error(`fail to load asset: ${assetId}`)
        }

        let asset = BaseModel.deserialize<Asset>(assetBuffer);
        if (!asset) {
            console.error(`fail to deserialize asset: ${assetId}`);
            throw new Error(`fail to deserialize asset: ${assetId}`)
        }
        let result = (new Asset(asset)).toJSON();
        console.log(`read asset success, assetId: ${assetId}, data: ${result}`);
        return result;
    }

    @Transaction(false)
    @Returns("string")
    public async ReadAssetPrivateDetails(ctx: Context, collect: string, assetId: string): Promise<string> {
        let assetDetailBuffer = await ctx.stub.getPrivateData(collect, assetId);
        if (!assetDetailBuffer || assetDetailBuffer.length === 0) {
            console.error(`fail to load asset detail: ${assetId}`);
            throw new Error(`fail to load asset detail: ${assetId}`)
        }

        let assetDetail = BaseModel.deserialize<AssetDetail>(assetDetailBuffer);
        if (!assetDetail) {
            console.error(`fail to deserialize asset detail: ${assetId}`);
            throw new Error(`fail to deserialize asset detail: ${assetId}`)
        }
        let result = (new AssetDetail(assetDetail)).toJSON();
        console.log(`read asset detail success, assetId: ${assetId}, data: ${result}`);
        return result;
    }

    @Transaction(false)
    @Returns("string")
    public async ReadTransferAgreement(ctx: Context, assetId: string): Promise<string|null> {
        let transferAgreeKey: string = ctx.stub.createCompositeKey(transferAgreementObjectType, [assetId]);
        let buyerIdentity = await ctx.stub.getPrivateData(assetCollection, transferAgreeKey);
        if (!buyerIdentity || buyerIdentity.length === 0) {
            console.error(`TransferAgreement for ${assetId} does not exist`);
            return null;
        }
        let agreement = new TransferAgreement(assetId, buyerIdentity.toString());
        return agreement.toJSON();
    }

    private submittingClientIdentity(ctx: Context): string | null {
        let base64ID = ctx.clientIdentity.getID();
        return base64ID
        // if (!base64ID || base64ID.length === 0) {
        //     return null;
        // }
        // return Buffer.from(base64ID, 'base64').toString();
    }

    private verifyClientOrgMatchesPeerOrg(ctx: Context): void {
        const clientMSPID: string = ctx.clientIdentity.getMSPID();
        const peerMSPID = ctx.stub.getMspID();
        if (peerMSPID != clientMSPID) {
            throw new Error(`client from org ${clientMSPID} is not authorized to read or write private data from an org ${peerMSPID} peer`);
        }
    }

    private getCollectionName(ctx: Context): string {
        const clientMSPID: string = ctx.clientIdentity.getMSPID();
        if (!clientMSPID || clientMSPID.length === 0) {
            throw new Error(`get client MSPID Error`);
        }
        return `${clientMSPID}PrivateCollection`;
    }

    private async verifyAgreement(ctx: Context, assetID: string, owner: string, buyerMSP: string) {

        // Check 1: verify that the transfer is being initiatied by the owner
        const clientId = this.submittingClientIdentity(ctx);
        if (!clientId) {
            throw new Error('cannot get clientId');
        } else if (clientId != owner) {
            throw new Error('error: submitting client identity does not own asset');
        }

        // Check 2: verify that the buyer has agreed to the appraised value
        const collectionOwner = this.getCollectionName(ctx);
        const collectionBuyer = `${buyerMSP}PrivateCollection`;

        const ownerAppraisedValueHash = await ctx.stub.getPrivateDataHash(collectionOwner, assetID);
        if (!ownerAppraisedValueHash || ownerAppraisedValueHash.length === 0) {
            throw new Error(`hash of appraised value for ${assetID} does not exist in collection ${collectionOwner}`);
        }

        const buyerAppraisedValueHash = await ctx.stub.getPrivateDataHash(collectionBuyer, assetID);
        if (!buyerAppraisedValueHash || buyerAppraisedValueHash.length === 0) {
            throw new Error(`hash of appraised value for ${assetID} does not exist in collection ${collectionBuyer}. `
                +`AgreeToTransfer must be called by the buyer first`)
        }

        if (ownerAppraisedValueHash.toString() != buyerAppraisedValueHash.toString()) {
            throw new Error(`hash for appraised value for owner ${ownerAppraisedValueHash.toString()}`+
            +` does not value for seller ${buyerAppraisedValueHash.toString()}`);
        }
    }
}
