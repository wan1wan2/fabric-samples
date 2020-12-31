import FabricCAServices from 'fabric-ca-client';
import { Gateway, GatewayOptions, Wallet } from 'fabric-network';
import * as AppUtils from './utils/AppUtils';
import * as CAUtils from './utils/CAUtils';
import path from 'path';

const mspOrg1 = 'Org1MSP';
const org1UserId = 'testAgent1';
const channelName = 'mychannel';
const chaincodeName = 'papercontract';
const gateway = new Gateway();
const walletPath = path.resolve(__dirname, '..', 'wallet');

async function main() {
    const ccp = AppUtils.buildOrg1();
    const caClient = CAUtils.buildCAClient(ccp, 'ca.org1.example.com');
    const wallet = await AppUtils.buildWallet(walletPath);
    await enrollUser(caClient, wallet);
    const contract = await buildContract(ccp, wallet);

    // create
    console.log(`*** Submit Create Function`);
    let createResult = await contract.submitTransaction('CreateBookingCommission', '1234567', '1000', org1UserId);
    console.log(`*** Result: ${AppUtils.prettyJSONString(createResult.toString())}`);

} 

async function enrollUser(caClient: FabricCAServices, wallet: Wallet) {
    
    let adminEnrollResult = await CAUtils.enrollAdmin(caClient, wallet, mspOrg1);
    if (!adminEnrollResult) throw new Error('enroll admin error');
    let appUserEnrollResult = await CAUtils.registerAndEnrollUser(caClient, wallet, mspOrg1
        , org1UserId, 'org1.department1');
    if (!appUserEnrollResult) throw new Error('enroll app user error');
}

async function buildContract(ccp: Record<string, any>, wallet: Wallet) {
    const gatewayOptions: GatewayOptions = {
        wallet,
        identity: org1UserId,
        discovery: {
            enabled: true,
            asLocalhost: true
        }
    };
    await gateway.connect(ccp, gatewayOptions);
    const network = await gateway.getNetwork(channelName);
    const contract = network.getContract(chaincodeName);
    return contract;
}

main();