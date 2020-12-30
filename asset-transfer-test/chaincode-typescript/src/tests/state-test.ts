import chai from 'chai';
const expect = chai.expect;


import { BookingCommission } from '../bookingCommission';
import { BOOKING_COMMISSION_NAMESPACE } from '../constants';
import { State } from '../state/state';

describe('test create state', () => {
    it('create state success', ()=> {
        let asset = new BookingCommission({
            bookingId: '1234567',
            agent: 'testAgent',
            commissionAmount: 0
        });
        expect(asset.bookingId === '1234567').true;
        expect(asset.agent === 'testAgent').true;
        expect(asset.commissionAmount === 0).true;
        expect(asset.status === 'OF').true;
        expect(asset.className === `${BOOKING_COMMISSION_NAMESPACE}.booking`).true;
        expect(asset.key).eq(State.makeyKey([asset.agent, asset.bookingId]));
    })
});
