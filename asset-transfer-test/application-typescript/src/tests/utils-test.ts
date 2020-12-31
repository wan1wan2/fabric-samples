import chai from 'chai';
import path from 'path';
const expect = chai.expect;
const should = chai.should();
import * as AppUtils from '../utils/AppUtils';
import * as CAUtils from '../utils/CAUtils';
const walletPath = path.resolve(__dirname, '..', '..', 'wallet');

describe('utils-test', ()=>{

    it('load CA should be success', ()=>{
        const result = AppUtils.buildOrg1();
        should.exist(result);
    });

    it('build memory wallet should be success', async ()=> {
        const result = await AppUtils.buildWallet();
        should.exist(result);
    });

    it('build fs wallet should be success', async ()=> {
        const wallet = await AppUtils.buildWallet(walletPath);
        should.exist(wallet);
    });

    it('enroll admin should be success', async ()=> {
        const ccp = AppUtils.buildOrg1();
        const caClient = CAUtils.buildCAClient(ccp, 'ca.org1.example.com');
        const mspOrg1 = 'Org1MSP';
        const wallet = await AppUtils.buildWallet();
        let result = await CAUtils.enrollAdmin(caClient, wallet, mspOrg1);
        expect(result).to.be.true;

    });

    it('register and enroll app user should be success', async ()=> {
        const ccp = AppUtils.buildOrg1();
        const caClient = CAUtils.buildCAClient(ccp, 'ca.org1.example.com');
        const mspOrg1 = 'Org1MSP';
        const wallet = await AppUtils.buildWallet();
        let adminEnrollResult = await CAUtils.enrollAdmin(caClient, wallet, mspOrg1);
        expect(adminEnrollResult).to.be.true;

        let appUserEnrollResult = await CAUtils.registerAndEnrollUser(caClient, wallet, mspOrg1
            , 'appUser', 'org1.department1');
        expect(appUserEnrollResult).to.be.true;

    });
}) 