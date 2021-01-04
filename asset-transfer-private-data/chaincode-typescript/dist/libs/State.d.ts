export declare class State {
    ID: string;
    serialize(): Uint8Array;
    static deserialize<T>(buffer: Uint8Array): T | null;
}
