import {Context, Contract, Info, Transaction} from 'fabric-contract-api';
import {Asset, AssetDetail} from './asset';

const assetCollection = "assetCollection"
// const transferAgreementObjectType = "transferAgreement"

@Info({title: 'AssetTransfer', description: 'Smart contract for trading assets'})
export class AssetTransferContract extends Contract {

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
        if (assetBytes) {
            console.log(`Asset already exists: ${transientAsset.ID}`);
            throw new Error(`this asset already exists: ${transientAsset.ID}`);
        }

        // Get ID of submitting client identity
        let clientId = this.submittingClientIdentity(ctx);
        if (!clientId || clientId.length === 0) {
            throw new Error(`get client Id error`);
        }

        this.verifyClientOrgMatchesPeerOrg(ctx);
        const assetInput = new Asset();
        assetInput.ID = transientAsset.ID;
        assetInput.Color = transientAsset.Color;
        assetInput.Size = transientAsset.Size;
        assetInput.docType = transientAsset.docType;
        assetInput.Owner = clientId;

        try {
            await ctx.stub.putPrivateData(assetCollection, assetInput.ID, assetInput.serialize());
        } catch (e) {
            throw new Error(`CreateAsset Error ${e}`);
        }

        try {
            const assetDetail: AssetDetail = new AssetDetail(assetInput.ID, transientAsset.AppraisedValue);
            const collectName = this.getCollectionName(ctx);
            await ctx.stub.putPrivateData(collectName, assetDetail.ID, assetDetail.serialize());
        } catch (e) {
            throw new Error(`CreateAssetDetail Error ${e}`);
        }
        
    }

    private submittingClientIdentity(ctx: Context): string | null {
        let base64ID = ctx.clientIdentity.getID();
        if (!base64ID || base64ID.length === 0) {
            return null;
        }
        return Buffer.from(base64ID, 'base64').toString();
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
