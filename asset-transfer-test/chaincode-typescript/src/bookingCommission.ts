import { Object, Property, Context } from 'fabric-contract-api';
import { State } from './state/state';
import { StateList } from './state/stateList';
import { BOOKING_COMMISSION_NAMESPACE } from './constants';

@Object()
export class BookingCommission extends State {

    @Property()
    public bookingId: string;

    @Property()
    public commissionAmount: number;

    @Property()
    public agent: string;

    @Property()
    public status: string;

    constructor(bookingId: string, agent: string, status: string, commissionAmount?: number) {
        super(BookingCommission.getClass(), [agent, bookingId])
        this.bookingId = bookingId;
        this.agent = agent;
        this.status = status;
        this.commissionAmount = commissionAmount || 0;
    }

    static getClass() {
        return `${BOOKING_COMMISSION_NAMESPACE}.booking`;
    }
}

export class BookingCommissionList extends StateList {
    constructor(ctx: Context) {
        super(ctx, BOOKING_COMMISSION_NAMESPACE);
        // this.use(BookingCommission)
    }

    
}