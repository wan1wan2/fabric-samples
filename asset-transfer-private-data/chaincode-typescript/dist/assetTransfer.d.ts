import { Context, Contract } from 'fabric-contract-api';
export declare class AssetTransferContract extends Contract {
    CreateAsset(ctx: Context): Promise<void>;
    ReadAsset(ctx: Context, assetId: string): Promise<string>;
    ReadAssetPrivateDetails(ctx: Context, collect: string, assetId: string): Promise<string>;
    private submittingClientIdentity;
    private verifyClientOrgMatchesPeerOrg;
    private getCollectionName;
}
