import { Property } from "fabric-contract-api";

export class State {
    @Property()
    public ID: string;

    public serialize() : Buffer {
        return Buffer.from(JSON.stringify(this));
    }
  
    public static deserialize<T>(buffer: Buffer) : T | null {
        let text = buffer.toString();
        if (!text || text.length === 0) {
            return null;
        }
        const data: T = JSON.parse(text);
        return data;
    }
}
