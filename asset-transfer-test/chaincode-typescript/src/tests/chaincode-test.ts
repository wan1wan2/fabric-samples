import chai from 'chai';
import sinon from 'sinon';
import sinonChai from 'sinon-chai';
import chaiPrmoise from 'chai-as-promised';
import { BookingCommissionTransferContract } from '../bookingCommissionTransfer';
import { ChaincodeStub } from 'fabric-shim';
import { BookingCommission } from '../bookingCommission';
import { State } from '../state/state';
const { Stub } = require('fabric-shim');

const expect = chai.expect;
const should = chai.should();
chai.use(sinonChai);
chai.use(chaiPrmoise);

describe('test chaincode', ()=> {
    let sandbox = sinon.createSandbox();
    let mockStubAPI: ChaincodeStub;

    beforeEach(()=>{
        mockStubAPI = sandbox.createStubInstance(Stub);
        mockStubAPI.createCompositeKey = (objectType: String, attributes: string[])=> {
            return `${objectType}:${attributes.join(':')}`;
        }
    });

    afterEach(() => {
        sandbox.restore();
    });

    describe('test booking commission contract', ()=> {
        const testBookingId = '1234567';
        const testAmount = 1000;
        const testAgent = 'testAgent1'

        it('create booking commission success', async ()=> {
            let contract = new BookingCommissionTransferContract();
            should.exist(contract);

            let ctx = contract.createContext();
            ctx.stub = mockStubAPI;
            should.exist(ctx.stub);

            let bookingCommission = await contract.CreateBookingCommission(ctx, testBookingId, testAmount, testAgent);
            should.exist(bookingCommission);
            expect(bookingCommission.bookingId).eq(testBookingId);
            expect(bookingCommission.agent).eq(testAgent);
            expect(bookingCommission.commissionAmount).eq(testAmount);
            expect(bookingCommission.status).eq('OF');
        });

        it('create booking commission should be error', async ()=> {
            let contract = new BookingCommissionTransferContract();
            should.exist(contract);

            let ctx = contract.createContext();
            ctx.stub = mockStubAPI;
            should.exist(ctx.stub);

            try {
                let bookingCommission = await contract.CreateBookingCommission(ctx, '', testAmount, '');
                expect(bookingCommission).eq(null);
            } catch (e) {
                should.exist(e);
            }
        });

        it('retrieve booking should be ok', async ()=> {
            let contract = new BookingCommissionTransferContract();
            should.exist(contract);

            let ctx = contract.createContext();
            ctx.stub = mockStubAPI;
            should.exist(ctx.stub);

            let bookingCommission = new BookingCommission({
                bookingId: testBookingId, 
                agent: testAgent, 
                status: 'OF', 
                commissionAmount: testAmount
            });
            let buffer = State.serialize(bookingCommission);
            mockStubAPI.getState = async ()=> {
                return buffer;
            };

            let retrieveBooking = await contract.RetrieveBookingCommission(ctx, testBookingId, testAgent);
            should.exist(retrieveBooking);
            expect(bookingCommission.bookingId).eq(retrieveBooking.bookingId);
            expect(bookingCommission.agent).eq(retrieveBooking.agent);
            expect(bookingCommission.commissionAmount).eq(retrieveBooking.commissionAmount);
        })

        it('update booking should be ok', async ()=> {
            let contract = new BookingCommissionTransferContract();
            should.exist(contract);

            let ctx = contract.createContext();
            ctx.stub = mockStubAPI;
            should.exist(ctx.stub);

            let bookingCommission = new BookingCommission({
                bookingId: testBookingId, 
                agent: testAgent, 
                status: 'OF', 
                commissionAmount: testAmount
            });
            let buffer = State.serialize(bookingCommission);
            mockStubAPI.getState = async ()=> {
                return buffer;
            };

            const newStatus = 'BK';
            let updatedBooking = await contract.UpdateBookingCommission(ctx
                , bookingCommission.bookingId, bookingCommission.commissionAmount, bookingCommission.agent
                , newStatus);
            should.exist(updatedBooking);
            expect(updatedBooking.bookingId).eq(bookingCommission.bookingId);
            expect(updatedBooking.status).eq(newStatus);

        })
    })
})