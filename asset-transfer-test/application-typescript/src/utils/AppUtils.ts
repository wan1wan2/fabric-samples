import path from 'path';
import fs from 'fs';
import { Wallet, Wallets } from 'fabric-network';

const buildOrg1 = (): Record<string, any> => {
    const ccpPath = path.resolve(__dirname, '..', '..', '..', '..', 'test-network',
        'organizations', 'peerOrganizations', 'org1.example.com', 'connection-org1.json');
    return loadCA(ccpPath);
}

const buildOrg2 = (): Record<string, any> => {
    const ccpPath = path.resolve(__dirname, '..', '..', '..', '..', 'test-network',
    'organizations', 'peerOrganizations', 'org2.example.com', 'connection-org2.json');
    return loadCA(ccpPath);
}

const buildWallet = async (walletPath?: string): Promise<Wallet> => {
    let wallet: Wallet;
    if (walletPath && walletPath.length > 0) {
        wallet = await Wallets.newFileSystemWallet(walletPath);
        console.log(`Built a file system wallet at ${walletPath}`);
    } else {
        wallet = await Wallets.newInMemoryWallet();
        console.log('Built an in memory wallet');
    }
    return wallet;
}

const prettyJSONString = (inputString: string): string => {
    if (inputString) {
         return JSON.stringify(JSON.parse(inputString), null, 2);
    } else {
         return inputString;
    }
};

export {
    buildOrg1,
    buildOrg2,
    buildWallet,
    prettyJSONString
}


function loadCA(ccpPath: string) {

    const fileExists = fs.existsSync(ccpPath);
    if (!fileExists) {
        throw new Error(`no such file or directory: ${ccpPath}`);
    }
    const contents = fs.readFileSync(ccpPath, 'utf8');

    // build a JSON object from the file contents
    const ccp = JSON.parse(contents);

    console.log(`Loaded the network configuration located at ${ccpPath}`);
    return ccp;
}
