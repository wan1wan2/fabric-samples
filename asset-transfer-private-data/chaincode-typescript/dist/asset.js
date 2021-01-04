"use strict";
/*
  SPDX-License-Identifier: Apache-2.0
*/
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
exports.AssetDetail = exports.Asset = void 0;
const fabric_contract_api_1 = require("fabric-contract-api");
const State_1 = require("./libs/State");
let Asset = class Asset extends State_1.State {
};
__decorate([
    fabric_contract_api_1.Property(),
    __metadata("design:type", String)
], Asset.prototype, "docType", void 0);
__decorate([
    fabric_contract_api_1.Property(),
    __metadata("design:type", String)
], Asset.prototype, "Color", void 0);
__decorate([
    fabric_contract_api_1.Property(),
    __metadata("design:type", Number)
], Asset.prototype, "Size", void 0);
__decorate([
    fabric_contract_api_1.Property(),
    __metadata("design:type", String)
], Asset.prototype, "Owner", void 0);
__decorate([
    fabric_contract_api_1.Property(),
    __metadata("design:type", Number)
], Asset.prototype, "AppraisedValue", void 0);
Asset = __decorate([
    fabric_contract_api_1.Object()
], Asset);
exports.Asset = Asset;
let AssetDetail = class AssetDetail extends State_1.State {
    constructor(id, appraisedValue) {
        super();
        this.ID = id;
        this.AppraisedValue = appraisedValue;
    }
};
__decorate([
    fabric_contract_api_1.Property(),
    __metadata("design:type", Number)
], AssetDetail.prototype, "AppraisedValue", void 0);
AssetDetail = __decorate([
    fabric_contract_api_1.Object(),
    __metadata("design:paramtypes", [String, Number])
], AssetDetail);
exports.AssetDetail = AssetDetail;
//# sourceMappingURL=asset.js.map