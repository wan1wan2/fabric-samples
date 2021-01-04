"use strict";
var __decorate = (this && this.__decorate) || function (decorators, target, key, desc) {
    var c = arguments.length, r = c < 3 ? target : desc === null ? desc = Object.getOwnPropertyDescriptor(target, key) : desc, d;
    if (typeof Reflect === "object" && typeof Reflect.decorate === "function") r = Reflect.decorate(decorators, target, key, desc);
    else for (var i = decorators.length - 1; i >= 0; i--) if (d = decorators[i]) r = (c < 3 ? d(r) : c > 3 ? d(target, key, r) : d(target, key)) || r;
    return c > 3 && r && Object.defineProperty(target, key, r), r;
};
var __metadata = (this && this.__metadata) || function (k, v) {
    if (typeof Reflect === "object" && typeof Reflect.metadata === "function") return Reflect.metadata(k, v);
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.State = void 0;
const fabric_contract_api_1 = require("fabric-contract-api");
class State {
    serialize() {
        return Buffer.from(JSON.stringify(this));
    }
    static deserialize(buffer) {
        let text = buffer.toString();
        if (!text || text.length === 0) {
            return null;
        }
        const data = JSON.parse(text);
        return data;
    }
}
__decorate([
    fabric_contract_api_1.Property(),
    __metadata("design:type", String)
], State.prototype, "ID", void 0);
exports.State = State;
//# sourceMappingURL=State.js.map