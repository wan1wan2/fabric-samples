import chai from 'chai';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import chaiPrmoise from 'chai-as-promised';
const { Stub } = require('fabric-shim');
import { ChaincodeStub, ClientIdentity } from 'fabric-shim';
import { AssetTransferContract } from '../assetTransfer';
import { Asset } from '../models/asset';

const expect = chai.expect;
const should = chai.should();
chai.use(sinonChai);
chai.use(chaiPrmoise);

const orgName = 'Org1MSP';

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
            await contract.CreateAsset(ctx, 'asset1', 'public desc');
        } catch (e) {
            expect(e).to.be.null;
        }
    });

    function getTestAsset(): Asset {
        let testAsset = new Asset('asset1', orgName, 'public desc');
        return testAsset
    }

})