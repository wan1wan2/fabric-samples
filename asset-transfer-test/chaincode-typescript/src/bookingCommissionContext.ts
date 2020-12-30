import { Context } from 'fabric-contract-api';
import { BookingCommissionList } from './bookingCommission';

export class BookingCommissionContext extends Context {
    bookingCommisionList: BookingCommissionList

    constructor() {
        super();
        this.bookingCommisionList = new BookingCommissionList(this);
    }
}