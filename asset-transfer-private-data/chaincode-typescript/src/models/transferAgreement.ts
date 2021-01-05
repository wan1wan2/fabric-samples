import {Object, Property} from 'fabric-contract-api';

@Object()
export class TransferAgreement {
    @Property()
    public ID: string;

    @Property()
    public BuyerID: string;

    constructor(id: string, buyerId: string) {
        this.ID = id;
        this.BuyerID = buyerId;
    }

    public serialize() : Uint8Array {
        return Buffer.from(this.toJSON());
      }
    
    public toJSON(): string {
        return JSON.stringify({
            ID: this.ID,
            BuyerID: this.BuyerID
        })
    }
}