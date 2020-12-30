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

    @Property()
    public createdTime: Date;

    @Property()
    public updatedTime: Date;

    constructor(obj: any) {
        super(BookingCommission.getClass(), [obj.agent, obj.bookingId])
        this.bookingId = obj.bookingId;
        this.agent = obj.agent;
        this.status = obj.status || 'OF';
        this.commissionAmount = obj.commissionAmount || 0;
        this.createdTime = obj.createdTime || new Date();
        this.updatedTime = obj.updatedTime || new Date();
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

    async addBookingCommission(bookingCommission: BookingCommission) {
        return this.addState(bookingCommission);
    }

    async updateBookingCommission(bookingCommission: BookingCommission) {
        return this.updateState(bookingCommission);
    }

    async getBookingCommission(key: string) {
        let data = await this.getState(key);
        if (!data) return null;
        return new BookingCommission(data);
    }

    async getAll() {
        let allResults = [];
        const iterator = await this.ctx.stub.getStateByRange('', '');
        let result = await iterator.next();
        while (!result.done) {
            const strValue = Buffer.from(result.value.value.toString()).toString('utf8');
            let record;
            try {
                record = JSON.parse(strValue);
            } catch (err) {
                console.log(err);
                record = strValue;
            }
            allResults.push({Key: result.value.key, Record: record});
            result = await iterator.next();
        }
        return allResults;
    }
}