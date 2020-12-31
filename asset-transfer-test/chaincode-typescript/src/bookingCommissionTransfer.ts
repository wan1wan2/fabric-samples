import { Info, Contract, Transaction, Returns } from 'fabric-contract-api';
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
    @Transaction()
    @Returns('string')
    public async CreateBookingCommission(ctx: BookingCommissionContext, bookingId: string
        , commissionAmount: number, agent: string): Promise<string> {

        if (!bookingId || bookingId.length == 0) {
            throw new Error('booking Id Empty');
        } else if (!agent || agent.length == 0) {
            throw new Error('agent error');
        }
        
        let booking = new BookingCommission({bookingId, agent, status: 'OF', commissionAmount});
        await ctx.bookingCommisionList.addBookingCommission(booking);
        return booking.serialize();
    }

    @Transaction(false)
    @Returns('boolean')
    public async IsBookingCommissionExists(ctx: BookingCommissionContext, bookingId: string, agent: string)
    : Promise<boolean> {
        try {
            let data = await this.RetrieveBookingCommission(ctx, bookingId, agent);
            return data != null;
        } catch (e) {
            console.error(e);
            return false;
        }
    }

    @Transaction(false)
    @Returns('string')
    public async RetrieveBookingCommission(ctx: BookingCommissionContext, bookingId: string, agent: string)
    : Promise<string> {
        const key = State.makeyKey([agent, bookingId]);
        let bookingCommission = await ctx.bookingCommisionList.getBookingCommission(key);
        if (bookingCommission == null) throw new Error(`booking: ${bookingId}, agent: ${agent} does not exists`);
        return bookingCommission.serialize();
    }

    @Transaction()
    @Returns('string')
    public async UpdateBookingCommission(ctx: BookingCommissionContext, bookingId: string
        , commissionAmount: number, agent: string, status: string): Promise<string> {
            let data = await this.RetrieveBookingCommission(ctx, bookingId, agent);
            if (!data) {
                throw new Error(`booking: ${bookingId}, agent: ${agent} does not exist`);
            }
            let bookingCommission = new BookingCommission(JSON.parse(data));
            bookingCommission.commissionAmount = commissionAmount;
            bookingCommission.status = status;
            bookingCommission.updatedTime = new Date();
            await ctx.bookingCommisionList.updateBookingCommission(bookingCommission);
            return bookingCommission.serialize();
        }
    
    // TODO Delete Booking Commission


    // TODO Retrieve Booking Commission By Booking Id

    
    // TODO Retrieve Booking Commission By Agent


    // TODO Retrieve Booking Commission All
    @Transaction(false)
    @Returns('string')
    public async RetrieveAll(ctx: BookingCommissionContext) {
        let results = await ctx.bookingCommisionList.getAll();
        return JSON.stringify(results);
    }
}