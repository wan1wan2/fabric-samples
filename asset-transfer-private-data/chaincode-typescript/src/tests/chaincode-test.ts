import chai from 'chai';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import chaiPrmoise from 'chai-as-promised';
const { Stub } = require('fabric-shim');
import { ChaincodeStub, ClientIdentity } from 'fabric-shim';
import { AssetTransferContract } from '../assetTransfer';
import { Asset, AssetDetail } from '../models/asset';
import { TransferAgreement } from '../models/transferAgreement';

const expect = chai.expect;
const should = chai.should();
chai.use(sinonChai);
chai.use(chaiPrmoise);

const orgName = 'Org1MSP';
const orgCollectionName = `${orgName}PrivateCollection`;
const assetCollection = "assetCollection"
const transferAgreementObjectType = "transferAgreement"

describe('test chaincode', ()=> {

    let sandbox = sinon.createSandbox();
    let mockStubAPI: ChaincodeStub;
    let mockClientIdentity: ClientIdentity;

    beforeEach(()=>{
        mockStubAPI = sandbox.createStubInstance(Stub);
        mockStubAPI.createCompositeKey = (objectType: string, attributes: string[]): string => {
            return `${objectType}_${attributes.join('_')}`;
        }
        mockStubAPI.getMspID = ()=> {
            return orgName
        }
        mockClientIdentity = sandbox.createStubInstance(ClientIdentity);
        mockClientIdentity.getMSPID = () => {
            return orgName
        }
        mockClientIdentity.getID = () => {
            let clientId = 'client';
            return clientId;
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
            result.set('asset_properties', Buffer.from(testAsset.toJSON()));
            return result;
        }

        try {
            await contract.CreateAsset(ctx);
        } catch (e) {
            expect(e).to.be.null;
        }

    });

    it('create asset should be failure, already exists', async ()=> {
        let contract = new AssetTransferContract();
        let ctx = contract.createContext();
        ctx.stub = mockStubAPI;
        ctx.clientIdentity = mockClientIdentity;
        mockStubAPI.getTransient = () => {
            let testAsset = getTestAsset();
            let result = new Map<string, Uint8Array>();
            result.set('asset_properties', Buffer.from(testAsset.toJSON()));
            return result;
        }

        mockStubAPI.getPrivateData = async (collection: string) => {
            if (collection === assetCollection) {
                let testAsset = getTestAsset();
                return testAsset.serialize();
            } 
            throw new Error('unsupported collection');
        }

        try {
            await contract.CreateAsset(ctx);
        } catch (e) {
            console.log(e);
            expect(e).to.not.null;
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

    it('agree transfer asset should be success', async ()=> {
        let contract = new AssetTransferContract();
        let ctx = contract.createContext();
        ctx.stub = mockStubAPI;
        ctx.clientIdentity = mockClientIdentity;
        mockStubAPI.getTransient = () => {
            let testAsset = getTestAsset();
            let result = new Map<string, Uint8Array>();
            result.set('asset_value', Buffer.from(testAsset.toJSON()));
            return result;
        }
        mockStubAPI.getPrivateData = async (collection: string) => {
            if (collection === assetCollection) {
                let testAsset = getTestAsset();
                return testAsset.serialize();
            } 
            throw new Error('unsupported collection');
        }

        try {
            await contract.AgreeTransfer(ctx);
        } catch (e) {
            console.log(e);
            expect(e).to.be.null;
        }

    });

    it('read transfer agreement should be success', async ()=> {
        let contract = new AssetTransferContract();
        let ctx = contract.createContext();
        ctx.stub = mockStubAPI;
        ctx.clientIdentity = mockClientIdentity;
        mockStubAPI.getPrivateData = async (collection: string) => {
            if (collection === assetCollection) {
                return Buffer.from('client');
            } 
            throw new Error('unsupported collection');
        }

        try {
            let result = await contract.ReadTransferAgreement(ctx, 'asset1');
            should.exist(result);
            expect(result).to.not.null;
            let jsonText: string = result || '';
            let transferObj = JSON.parse(jsonText);
            let transferAgreement: TransferAgreement = new TransferAgreement(transferObj.ID, transferObj.BuyerID);
            expect(transferAgreement.ID).eq('asset1');
            expect(transferAgreement.BuyerID).eq('client');

        } catch (e) {
            console.log(e);
            expect(e).to.be.null;
        }

    });

    it('transfer asset should be success', async ()=> {
        let contract = new AssetTransferContract();
        let ctx = contract.createContext();
        ctx.stub = mockStubAPI;
        ctx.clientIdentity = mockClientIdentity;
        mockStubAPI.getPrivateData = async (collection: string, key: string) => {
            if (collection === assetCollection) {
                if (key.indexOf(transferAgreementObjectType) >= 0) {
                    return Buffer.from('client');
                } else {
                    let asset = getTestAsset();
                    return asset.serialize(); 
                }
            } 
            throw new Error('unsupported collection');
        }
        mockStubAPI.getPrivateDataHash = async () => {
            return Buffer.from('client');
        }

        interface TransientTransferInput {
            assetID: string,
            buyerMSP: string
        }

        mockStubAPI.getTransient = () => {
            let result = new Map<string, Uint8Array>();
            let transientInput: TransientTransferInput = {
                assetID: 'asset1',
                buyerMSP: orgName
            }   
            result.set('asset_owner', Buffer.from(JSON.stringify(transientInput)));
            return result;
        }

        try {
            await contract.TransferAsset(ctx);
        } catch (e) {
            console.error(e);
            expect(e).to.be.null;
        }

    });

    function getTestAsset(): Asset {
        let testAsset = new Asset({
            ID: 'asset1',
            AppraisedValue: 200,
            Color: 'blue',
            Size: 16,
            docType: 'asset',
            Owner: 'client'
        });
        return testAsset
    }

    function getTestAssetDetails(): AssetDetail {
        let testAssetDetail = new AssetDetail({
            ID: 'asset1',
            AppraisedValue: 200
        });
        return testAssetDetail;
    }

})