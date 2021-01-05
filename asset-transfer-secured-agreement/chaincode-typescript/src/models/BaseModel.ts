import { Property } from "fabric-contract-api";

export abstract class BaseModel {
    @Property()
    public ID: string;

    constructor(ID: string) {
        this.ID = ID;
    }

    public serialize() : Uint8Array {
        return Buffer.from(this.toJSON());
    }
  
    public static deserialize<T>(buffer: Uint8Array) : T | null {
        let text = buffer.toString();
        if (!text || text.length === 0) {
            return null;
        }
        const data: T = JSON.parse(text);
        return data;
    }

    public abstract toJSON(): string;
}
