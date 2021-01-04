/*
  SPDX-License-Identifier: Apache-2.0
*/

import {Object, Property} from 'fabric-contract-api';
import { BaseModel } from './BaseModel';

@Object()
export class Asset extends BaseModel {
    @Property()
    public docType?: string;

    @Property()
    public Color: string;

    @Property()
    public Size: number;

    @Property()
    public Owner: string;

    @Property()
    public AppraisedValue: number;

    constructor(object: any) {
      super(object.ID);
      this.AppraisedValue = object.AppraisedValue;
      this.docType = object.docType;
      this.Color = object.Color;
      this.Size = object.Size;
      this.Owner = object.Owner;
    }

    public toJSON(): string {
      return JSON.stringify({
        ID: this.ID,
        docType: this.docType,
        Color: this.Color,
        Size: this.Size,
        Owner: this.Owner,
        AppraisedValue: this.AppraisedValue
      })
    }
}

@Object()
export class AssetDetail extends BaseModel {

    @Property()
    public AppraisedValue: number

    constructor(object: any) {
      super(object.ID);
      this.AppraisedValue = object.AppraisedValue;
    }

    public toJSON(): string {
      return JSON.stringify({
        ID: this.ID,
        AppraisedValue: this.AppraisedValue
      })
    }
}
