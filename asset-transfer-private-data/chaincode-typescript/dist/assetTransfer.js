"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.AssetTransferContract = void 0;
const fabric_contract_api_1 = require("fabric-contract-api");
const asset_1 = require("./asset");
const assetCollection = "assetCollection";
// const transferAgreementObjectType = "transferAgreement"
let AssetTransferContract = class AssetTransferContract extends fabric_contract_api_1.Contract {
    async CreateAsset(ctx) {
        const transientMap = ctx.stub.getTransient();
        const transientAssetJson = transientMap.get('asset_properties');
        if (!transientAssetJson) {
            throw new Error(`asset not found in the transient map input`);
        }
        const transientAsset = JSON.parse(transientAssetJson.toString());
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
        const assetInput = new asset_1.Asset();
        assetInput.ID = transientAsset.ID;
        assetInput.Color = transientAsset.Color;
        assetInput.Size = transientAsset.Size;
        assetInput.docType = transientAsset.docType;
        assetInput.Owner = clientId;
        try {
            await ctx.stub.putPrivateData(assetCollection, assetInput.ID, assetInput.serialize());
        }
        catch (e) {
            throw new Error(`CreateAsset Error ${e}`);
        }
        try {
            const assetDetail = new asset_1.AssetDetail(assetInput.ID, transientAsset.AppraisedValue);
            const collectName = this.getCollectionName(ctx);
            await ctx.stub.putPrivateData(collectName, assetDetail.ID, assetDetail.serialize());
        }
        catch (e) {
            throw new Error(`CreateAssetDetail Error ${e}`);
        }
    }
    submittingClientIdentity(ctx) {
        let base64ID = ctx.clientIdentity.getID();
        if (!base64ID || base64ID.length === 0) {
            return null;
        }
        return Buffer.from(base64ID, 'base64').toString();
    }
    verifyClientOrgMatchesPeerOrg(ctx) {
        const clientMSPID = ctx.clientIdentity.getMSPID();
        const peerMSPID = ctx.stub.getMspID();
        if (peerMSPID != clientMSPID) {
            throw new Error(`client from org ${clientMSPID} is not authorized to read or write private data from an org ${peerMSPID} peer`);
        }
    }
    getCollectionName(ctx) {
        const clientMSPID = ctx.clientIdentity.getMSPID();
        if (!clientMSPID || clientMSPID.length === 0) {
            throw new Error(`get client MSPID Error`);
        }
        return `${clientMSPID}PrivateCollection`;
    }
};
__decorate([
    fabric_contract_api_1.Transaction(),
    __metadata("design:type", Function),
    __metadata("design:paramtypes", [fabric_contract_api_1.Context]),
    __metadata("design:returntype", Promise)
], AssetTransferContract.prototype, "CreateAsset", null);
AssetTransferContract = __decorate([
    fabric_contract_api_1.Info({ title: 'AssetTransfer', description: 'Smart contract for trading assets' })
], AssetTransferContract);
exports.AssetTransferContract = AssetTransferContract;
//# sourceMappingURL=assetTransfer.js.map