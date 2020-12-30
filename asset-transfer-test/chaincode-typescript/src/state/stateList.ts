import { Context } from 'fabric-contract-api';
import { State } from './state';

export class StateList {
    ctx: Context;
    name: string;

    constructor(ctx: Context, listName: string) {
        this.ctx = ctx;
        this.name = listName;
    }

    async add(state: State) {
        let key = this.ctx.stub.createCompositeKey(this.name, state.getSplitKey());
        let data = State.serialize(state);
        await this.ctx.stub.putState(key, data);
    }

    /**
     * Get a state from the list using supplied keys. Form composite
     * keys to retrieve state from world state. State data is deserialized
     * into JSON object before being returned.
     */
    async getState(key: string) {
        let ledgerKey = this.ctx.stub.createCompositeKey(this.name, State.splitKey(key));
        let data = await this.ctx.stub.getState(ledgerKey);
        if (data && data.toString()) {
            let state = State.deserialize(data);
            return state;
        } else {
            return null;
        }
    }

    async updateState(state: State) {
        let key = this.ctx.stub.createCompositeKey(this.name, state.getSplitKey());
        let data = State.serialize(state);
        await this.ctx.stub.putState(key, data);
    }
}