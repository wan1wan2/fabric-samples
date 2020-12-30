export class State {
    className: string;
    key: string;

    constructor(className: string, keyParts: string[]) {
        this.className = className;
        this.key = keyParts.join(':');
    }

    getSplitKey() {
        return State.splitKey(this.key);
    }
    
    static splitKey(key: string) {
        return key.split(':')
    }

    /**
     * Convert object to buffer containing JSON data serialization
     * Typically used before putState()ledger API
     * @param {Object} JSON object to serialize
     * @return {buffer} buffer with the data to store
     */
    static serialize(object: any) {
        // don't write the key:value passed in - we already have a real composite key, issuer and paper Number.
        delete object.key;
        return Buffer.from(JSON.stringify(object));
    }

    /**
     * Deserialize object into one of a set of supported JSON classes
     * i.e. Covert serialized data to JSON object
     * Typically used after getState() ledger API
     * @param {data} data to deserialize into JSON object
     * @param (supportedClasses) the set of classes data can be serialized to
     * @return {json} json with the data to store
     */
    static deserialize(data: any) {
        let json = JSON.parse(data.toString());
        let object = new (json);
        return object;
    }

    
}