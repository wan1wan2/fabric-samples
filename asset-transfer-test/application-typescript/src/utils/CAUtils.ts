import FabricCAServices from "fabric-ca-client";
import { Wallet } from "fabric-network";

const adminUserId = 'admin';
const adminUserPasswd = 'adminpw';

const buildCAClient = (ccp: Record<string, any>, caHostName: string): FabricCAServices => {
    // Create a new CA client for interacting with the CA.
    const caInfo = ccp.certificateAuthorities[caHostName]; // lookup CA details from config
    const caTLSCACerts = caInfo.tlsCACerts.pem;
    const caClient = new FabricCAServices(caInfo.url, { trustedRoots: caTLSCACerts, verify: false }, caInfo.caName);

    console.log(`Built a CA Client named ${caInfo.caName}`);
    return caClient;
}

const enrollAdmin = async (caClient: FabricCAServices, wallet: Wallet, orgMspId: string): Promise<Boolean> => {
    try {
        const identity = await wallet.get(adminUserId);
        if (identity) {
            console.log('An identity for the admin user already exists in the wallet');
            return true;
        }

        const enrollment = await caClient.enroll({
            enrollmentID: adminUserId,
            enrollmentSecret: adminUserPasswd
        });
        const x509Identity = {
            credentials: {
                certificate: enrollment.certificate,
                privateKey: enrollment.key.toBytes(),
            },
            mspId: orgMspId,
            type: 'X.509',
        };
        await wallet.put(adminUserId, x509Identity);
        console.log('Successfully enrolled admin user and imported it into the wallet');
        return true;
    } catch (e) {
        console.error(`Failed to enroll admin user : ${e}`);
        return false;
    }
}

const registerAndEnrollUser = async (caClient: FabricCAServices, wallet: Wallet, orgMspId: string
    , userId: string, affiliation: string): Promise<Boolean> => {

    try {
        const identity = await wallet.get(userId);
        if (identity) {
            console.log('An identity for the admin user already exists in the wallet');
            return true;
        }

        // Must use an admin to register a new user
        const adminIdentity = await wallet.get(adminUserId);
        if (!adminIdentity) {
            console.log('An identity for the admin user does not exist in the wallet');
            console.log('Enroll the admin user before retrying');
            return false;
        }

        const provider = wallet.getProviderRegistry().getProvider(adminIdentity.type);
        const adminUser = await provider.getUserContext(adminIdentity, adminUserId);
        const secret = await caClient.register({
            affiliation,
            enrollmentID: userId,
            role: 'client'
        }, adminUser);

        const enrollment = await caClient.enroll({
            enrollmentID: userId,
            enrollmentSecret: secret,
        });
        const x509Identity = {
            credentials: {
                certificate: enrollment.certificate,
                privateKey: enrollment.key.toBytes(),
            },
            mspId: orgMspId,
            type: 'X.509',
        };
        await wallet.put(userId, x509Identity);
        console.log(`Successfully registered and enrolled user ${userId} and imported it into the wallet`);

        return true;
    } catch (e) {
        console.error(`Failed to enroll user ${userId} : ${e}`);
    return false;
    }
}

export {
    buildCAClient,
    enrollAdmin,
    registerAndEnrollUser
}