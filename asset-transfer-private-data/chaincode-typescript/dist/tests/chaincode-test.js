"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const chai_1 = __importDefault(require("chai"));
const sinon_1 = __importDefault(require("sinon"));
const sinon_chai_1 = __importDefault(require("sinon-chai"));
const chai_as_promised_1 = __importDefault(require("chai-as-promised"));
const { Stub } = require('fabric-shim');
const fabric_shim_1 = require("fabric-shim");
const assetTransfer_1 = require("../assetTransfer");
const asset_1 = require("../asset");
const expect = chai_1.default.expect;
const should = chai_1.default.should();
chai_1.default.use(sinon_chai_1.default);
chai_1.default.use(chai_as_promised_1.default);
const orgName = 'Org1MSP';
const orgCollectionName = `${orgName}PrivateCollection`;
const assetCollection = "assetCollection";
describe('test chaincode', () => {
    let sandbox = sinon_1.default.createSandbox();
    let mockStubAPI;
    let mockClientIdentity;
    beforeEach(() => {
        mockStubAPI = sandbox.createStubInstance(Stub);
        mockStubAPI.getMspID = () => {
            return orgName;
        };
        mockClientIdentity = sandbox.createStubInstance(fabric_shim_1.ClientIdentity);
        mockClientIdentity.getMSPID = () => {
            return orgName;
        };
        mockClientIdentity.getID = () => {
            let clientId = 'admin';
            return Buffer.from(clientId, 'ascii').toString('base64');
        };
    });
    afterEach(() => {
        sandbox.restore();
    });
    it('init should be success', async () => {
        let contract = new assetTransfer_1.AssetTransferContract();
        should.exist(contract);
        let ctx = contract.createContext();
        ctx.stub = mockStubAPI;
        ctx.clientIdentity = mockClientIdentity;
        should.exist(ctx.stub);
    });
    it('create asset should be success', async () => {
        let contract = new assetTransfer_1.AssetTransferContract();
        let ctx = contract.createContext();
        ctx.stub = mockStubAPI;
        ctx.clientIdentity = mockClientIdentity;
        mockStubAPI.getTransient = () => {
            let testAsset = getTestAsset();
            let result = new Map();
            result.set('asset_properties', Buffer.from(JSON.stringify(testAsset)));
            return result;
        };
        try {
            await contract.CreateAsset(ctx);
        }
        catch (e) {
            expect(e).to.be.null;
        }
    });
    it('read asset should be success', async () => {
        let contract = new assetTransfer_1.AssetTransferContract();
        let ctx = contract.createContext();
        ctx.stub = mockStubAPI;
        ctx.clientIdentity = mockClientIdentity;
        mockStubAPI.getPrivateData = async (collection) => {
            if (collection === assetCollection) {
                let testAsset = getTestAsset();
                return testAsset.serialize();
            }
            throw new Error('unsupported collection');
        };
        try {
            let assetText = await contract.ReadAsset(ctx, 'asset1');
            expect(assetText && assetText.length > 0).to.be.true;
        }
        catch (e) {
            expect(e).to.be.null;
        }
    });
    it('read asset detail should be success', async () => {
        let contract = new assetTransfer_1.AssetTransferContract();
        let ctx = contract.createContext();
        ctx.stub = mockStubAPI;
        ctx.clientIdentity = mockClientIdentity;
        mockStubAPI.getPrivateData = async (collection) => {
            if (collection === collection) {
                let testAsset = getTestAssetDetails();
                return testAsset.serialize();
            }
            throw new Error('unsupported collection');
        };
        try {
            let assetText = await contract.ReadAssetPrivateDetails(ctx, orgCollectionName, 'asset1');
            expect(assetText && assetText.length > 0).to.be.true;
        }
        catch (e) {
            expect(e).to.be.null;
        }
    });
    function getTestAsset() {
        let testAsset = new asset_1.Asset();
        testAsset.AppraisedValue = 200;
        testAsset.Color = 'blue';
        testAsset.ID = 'asset1';
        testAsset.Size = 16;
        testAsset.docType = 'asset';
        return testAsset;
    }
    function getTestAssetDetails() {
        let testAssetDetail = new asset_1.AssetDetail('asset1', 200);
        return testAssetDetail;
    }
});
//# sourceMappingURL=chaincode-test.js.map