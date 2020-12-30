import { Info, Contract, Context } from 'fabric-contract-api';
import { BookingCommission } from './bookingCommission';
import { BookingCommissionContext } from './bookingCommissionContext';
import { BOOKING_COMMISSION_NAMESPACE } from './constants';

@Info({title: 'BookingCommissionTransfer', description: 'Smart contract for apply booking commission'})
export class BookingCommissionTransferContract extends Contract {

    constructor() {
        super(BOOKING_COMMISSION_NAMESPACE);
    }

    createContext(): Context {
        return new BookingCommissionContext();
    }

    // Create Booking Commission
    public async CreateBookingCommission(ctx: BookingCommissionContext, bookingId: string
        , commissionAmount: number, agent: string): Promise<BookingCommission> {

            if (!bookingId || bookingId.length == 0) {
                throw new Error('booking Id Empty');
            } else if (!agent || agent.length == 0) {
                throw new Error('agent error');
            }
            
            let booking = new BookingCommission(bookingId, agent, 'OF', commissionAmount);
            await ctx.bookingCommisionList.addBookingCommission(booking);
            return booking;
        }

    // TODO Update Booking Commission

    
    // TODO Delete Booking Commission


    // TODO Retrieve Booking Commission By Booking Id

    
    // TODO Retrieve Booking Commission By Agent


    // TODO Retrieve Booking Commission All
}