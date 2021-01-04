import { Context, Contract } from 'fabric-contract-api';
export declare class AssetTransferContract extends Contract {
    CreateAsset(ctx: Context): Promise<void>;
    private submittingClientIdentity;
    private verifyClientOrgMatchesPeerOrg;
    private getCollectionName;
}
