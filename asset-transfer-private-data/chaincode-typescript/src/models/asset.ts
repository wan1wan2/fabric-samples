/*
  SPDX-License-Identifier: Apache-2.0
*/

import {Object, Property} from 'fabric-contract-api';

@Object()
export class Asset {

  @Property()
  public ID: string;

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
    this.ID = object.ID;
    this.AppraisedValue = object.AppraisedValue;
    this.docType = object.docType;
    this.Color = object.Color;
    this.Size = object.Size;
    this.Owner = object.Owner;
  }

  public serialize() : Uint8Array {
    return Buffer.from(this.toJSON());
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
export class AssetDetail {

  @Property()
  public ID: string;

  @Property()
  public AppraisedValue: number;

  constructor(object: any) {
    this.ID = object.ID;
    this.AppraisedValue = object.AppraisedValue;
  }

  public serialize() : Uint8Array {
    return Buffer.from(this.toJSON());
  }

  public toJSON(): string {
    return JSON.stringify({
      ID: this.ID,
      AppraisedValue: this.AppraisedValue
    })
  }
}
