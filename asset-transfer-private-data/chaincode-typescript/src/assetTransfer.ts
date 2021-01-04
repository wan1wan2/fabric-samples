import {Context, Contract, Info, Returns, Transaction} from 'fabric-contract-api';
import {Asset, AssetDetail} from './models/asset';
import { BaseModel } from './models/BaseModel';

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
}
