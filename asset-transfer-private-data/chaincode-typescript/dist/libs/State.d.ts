/// <reference types="node" />
export declare class State {
    ID: string;
    serialize(): Buffer;
    static deserialize<T>(buffer: Buffer): T | null;
}
