import { Property } from "fabric-contract-api";

export class State {
    @Property()
    public ID: string;

    public serialize() : Uint8Array {
        return Buffer.from(JSON.stringify(this));
    }
  
    public static deserialize<T>(buffer: Uint8Array) : T | null {
        let text = buffer.toString();
        if (!text || text.length === 0) {
            return null;
        }
        const data: T = JSON.parse(text);
        return data;
    }
}
