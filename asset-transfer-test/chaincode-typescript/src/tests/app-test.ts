import test, { ExecutionContext } from 'ava';
import { BookingCommission } from '../bookingCommission';
import { BOOKING_COMMISSION_NAMESPACE } from '../constants';

test('booking commission constructor success', (t: ExecutionContext) => {
    let asset = new BookingCommission('1234567', 'testAgent', 'OF', 0);
    t.true(asset.bookingId === '1234567');
    t.true(asset.agent === 'testAgent');
    t.true(asset.commissionAmount === 0);
    t.true(asset.status === 'OF');
    t.true(asset.className === `${BOOKING_COMMISSION_NAMESPACE}.booking`);
    t.true(asset.key != null);
});