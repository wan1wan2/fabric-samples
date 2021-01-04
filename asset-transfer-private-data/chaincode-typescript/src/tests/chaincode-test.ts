import chai from 'chai';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import chaiPrmoise from 'chai-as-promised';
const { Stub } = require('fabric-shim');
import { ChaincodeStub, ClientIdentity } from 'fabric-shim';
import { AssetTransferContract } from '../assetTransfer';
import { Asset, AssetDetail } from '../asset';

const expect = chai.expect;
const should = chai.should();
chai.use(sinonChai);
chai.use(chaiPrmoise);

const orgName = 'Org1MSP';
const orgCollectionName = `${orgName}PrivateCollection`;
const assetCollection = "assetCollection";

describe('test chaincode', ()=> {

    let sandbox = sinon.createSandbox();
    let mockStubAPI: ChaincodeStub;
    let mockClientIdentity: ClientIdentity;

    beforeEach(()=>{
        mockStubAPI = sandbox.createStubInstance(Stub);
        mockStubAPI.getMspID = ()=> {
            return orgName
        }
        mockClientIdentity = sandbox.createStubInstance(ClientIdentity);
        mockClientIdentity.getMSPID = () => {
            return orgName
        }
        mockClientIdentity.getID = () => {
            let clientId = 'admin';
            return Buffer.from(clientId, 'ascii').toString('base64');
        }
    });

    afterEach(() => {
        sandbox.restore();
    });

    it('init should be success', async ()=> {
        let contract = new AssetTransferContract();
        should.exist(contract);

        let ctx = contract.createContext();
        ctx.stub = mockStubAPI;
        ctx.clientIdentity = mockClientIdentity;
        should.exist(ctx.stub);
    });

    it('create asset should be success', async ()=> {
        let contract = new AssetTransferContract();
        let ctx = contract.createContext();
        ctx.stub = mockStubAPI;
        ctx.clientIdentity = mockClientIdentity;
        mockStubAPI.getTransient = () => {
            let testAsset = getTestAsset();
            let result = new Map<string, Uint8Array>();
            result.set('asset_properties', Buffer.from(JSON.stringify(testAsset)));
            return result;
        }

        try {
            await contract.CreateAsset(ctx);
        } catch (e) {
            expect(e).to.be.null;
        }

    });

    it('read asset should be success', async ()=> {
        let contract = new AssetTransferContract();
        let ctx = contract.createContext();
        ctx.stub = mockStubAPI;
        ctx.clientIdentity = mockClientIdentity;
        mockStubAPI.getPrivateData = async (collection: string) => {
            if (collection === assetCollection) {
                let testAsset = getTestAsset();
                return testAsset.serialize();
            } 
            throw new Error('unsupported collection');
        }
        
        try {
            let assetText = await contract.ReadAsset(ctx, 'asset1');
            expect(assetText && assetText.length > 0).to.be.true;
        } catch (e) {
            expect(e).to.be.null;
        }

    });

    it('read asset detail should be success', async ()=> {
        let contract = new AssetTransferContract();
        let ctx = contract.createContext();
        ctx.stub = mockStubAPI;
        ctx.clientIdentity = mockClientIdentity;
        mockStubAPI.getPrivateData = async (collection: string) => {
            if (collection === collection) {
                let testAsset = getTestAssetDetails();
                return testAsset.serialize();
            } 
            throw new Error('unsupported collection');
        }
        
        try {
            let assetText = await contract.ReadAssetPrivateDetails(ctx, orgCollectionName, 'asset1');
            expect(assetText && assetText.length > 0).to.be.true;
        } catch (e) {
            expect(e).to.be.null;
        }

    });

    function getTestAsset(): Asset {
        let testAsset = new Asset();
        testAsset.AppraisedValue = 200;
        testAsset.Color = 'blue';
        testAsset.ID = 'asset1';
        testAsset.Size = 16;
        testAsset.docType = 'asset';
        return testAsset
    }

    function getTestAssetDetails(): AssetDetail {
        let testAssetDetail = new AssetDetail('asset1', 200);
        return testAssetDetail;
    }

})