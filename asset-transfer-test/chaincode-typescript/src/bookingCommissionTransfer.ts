import { Info, Contract } from 'fabric-contract-api';
import { BookingCommission } from './bookingCommission';
import { BookingCommissionContext } from './bookingCommissionContext';
import { BOOKING_COMMISSION_NAMESPACE } from './constants';
import { State } from './state/state';

@Info({title: 'BookingCommissionTransfer', description: 'Smart contract for apply booking commission'})
export class BookingCommissionTransferContract extends Contract {

    constructor() {
        super(BOOKING_COMMISSION_NAMESPACE);
    }

    createContext(): BookingCommissionContext {
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
            
            let booking = new BookingCommission({bookingId, agent, status: 'OF', commissionAmount});
            await ctx.bookingCommisionList.addBookingCommission(booking);
            return booking;
        }

    public async RetrieveBookingCommission(ctx: BookingCommissionContext, bookingId: string, agent: string)
    : Promise<BookingCommission> {
        const key = State.makeyKey([agent, bookingId]);
        let bookingCommission = await ctx.bookingCommisionList.getBookingCommission(key);
        if (bookingCommission == null) throw new Error(`booking: ${bookingId}, agent: ${agent} does not exists`);
        return bookingCommission;
    }

    public async UpdateBookingCommission(ctx: BookingCommissionContext, bookingId: string
        , commissionAmount: number, agent: string, status: string): Promise<BookingCommission> {
            let bookingCommission = await this.RetrieveBookingCommission(ctx, bookingId, agent);
            if (!bookingCommission) {
                throw new Error(`booking: ${bookingId}, agent: ${agent} does not exist`);
            }
            bookingCommission.commissionAmount = commissionAmount;
            bookingCommission.status = status;
            bookingCommission.updatedTime = new Date();
            await ctx.bookingCommisionList.updateBookingCommission(bookingCommission);
            return bookingCommission;
        }
    
    // TODO Delete Booking Commission


    // TODO Retrieve Booking Commission By Booking Id

    
    // TODO Retrieve Booking Commission By Agent


    // TODO Retrieve Booking Commission All
    public async RetrieveAll(ctx: BookingCommissionContext) {
        return await ctx.bookingCommisionList.getAll();
    }
}