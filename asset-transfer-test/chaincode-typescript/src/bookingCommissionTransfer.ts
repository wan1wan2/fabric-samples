// import { BookingCommission } from './bookingCommission';
import { Info, Contract, Context } from 'fabric-contract-api';
import { BookingCommission } from './bookingCommission';
import { BOOKING_COMMISSION_NAMESPACE } from './constants';

@Info({title: 'BookingCommissionTransfer', description: 'Smart contract for apply booking commission'})
export class BookingCommissionTransferContract extends Contract {

    constructor() {
        super(BOOKING_COMMISSION_NAMESPACE);
    }

    // Create Booking Commission
    public async CreateBookingCommission(ctx: Context, bookingId: string
        , commissionAmount: number, agent: string): Promise<void> {

            if (!bookingId || bookingId.length == 0) {
                throw new Error('booking Id Empty');
            } else if (!agent || agent.length == 0) {
                throw new Error('agent error');
            }
            
            new BookingCommission(bookingId, agent, 'OF', commissionAmount);

        }

    // TODO Update Booking Commission

    
    // TODO Delete Booking Commission


    // TODO Retrieve Booking Commission By Booking Id

    
    // TODO Retrieve Booking Commission By Agent


    // TODO Retrieve Booking Commission All
}