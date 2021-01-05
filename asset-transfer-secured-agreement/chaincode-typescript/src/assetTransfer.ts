import {Context, Contract, Info, Returns, Transaction} from 'fabric-contract-api';
import { KeyEndorsementPolicy } from 'fabric-shim';
import { Asset, IAsset } from './models/asset';


@Info({title: 'AssetTransfer', description: 'Smart contract for trading assets'})
export class AssetTransferContract extends Contract {

    @Transaction(false)
    public TestConnection(): string {
        return 'connection success';
    }

    @Transaction()
    public async CreateAsset(ctx: Context, assetID: string, publicDescription: string): Promise<void> {
        const transientMap = ctx.stub.getTransient();
        const immutablePropertiesJSON = transientMap.get('asset_properties');
        if (!immutablePropertiesJSON || immutablePropertiesJSON.length === 0) {
            throw new Error("asset_properties key not found in the transient map");
        }
        const clientOrgID = this.getClientOrgID(ctx, true);
        let asset: Asset = new Asset(assetID, clientOrgID, publicDescription);
        await ctx.stub.putState(asset.ID, asset.serialize());

        try {
            this.setAssetStateBasedEndorsement(ctx, assetID, clientOrgID);
        } catch (e) {
            throw new Error(`failed setting state based endorsement for owner ${e}`);
        }

        const collection = this.buildCollectionName(clientOrgID);
        await ctx.stub.putPrivateData(collection, asset.ID, immutablePropertiesJSON);
    }


    @Transaction(false)
    @Returns("string")
    public async ReadAsset(ctx: Context, assetID: string): Promise<Asset> {
        let assetJSON = await ctx.stub.getState(assetID);
        if (!assetJSON || assetJSON.length === 0) {
            throw new Error(`${assetID} does not exist`);
        }
        let asset: IAsset = JSON.parse(assetJSON.toString());
        return Asset.Create(asset);
    }

    private getClientOrgID(ctx: Context, verifyOrg: boolean): string {
        const clientOrgID = ctx.clientIdentity.getMSPID();
        if (verifyOrg) {
            this.verifyClientOrgMatchesPeerOrg(ctx);
        }
        return clientOrgID;
    }


    private verifyClientOrgMatchesPeerOrg(ctx: Context): void {
        const clientMSPID: string = ctx.clientIdentity.getMSPID();
        const peerMSPID = ctx.stub.getMspID();
        if (peerMSPID != clientMSPID) {
            throw new Error(`client from org ${clientMSPID} is not authorized to read or write private data from an org ${peerMSPID} peer`);
        }
    }

    
    // setAssetStateBasedEndorsement adds an endorsement policy to a asset so that only a peer from an owning org
    // can update or transfer the asset.
    private async setAssetStateBasedEndorsement(ctx: Context, assetID: string, orgToEndorse: string) {
        let endorsementPolicy: KeyEndorsementPolicy = new KeyEndorsementPolicy();
        
        try {
            endorsementPolicy.addOrgs("PEER", orgToEndorse)
        } catch (e) {
            throw new Error(`failed to add org to endorsement policy ${e}`);
        }
        let policy = endorsementPolicy.getPolicy()
        await ctx.stub.setStateValidationParameter(assetID, policy);
    }

    private buildCollectionName(clientOrgID: string): string {
        return `_implicit_org_${clientOrgID}`;
    }

    // private getClientImplicitCollectionName(ctx: Context): string {
    //     const clientOrgID = this.getClientOrgID(ctx, true)
    //     if (!clientOrgID || clientOrgID.length === 0) {
    //         throw new Error('client org id empty');
    //     }

    //     this.verifyClientOrgMatchesPeerOrg(ctx);

    //     return this.buildCollectionName(clientOrgID);
    // }

}
