/*
  SPDX-License-Identifier: Apache-2.0
*/

import {Object, Property} from 'fabric-contract-api';

@Object()
export class Asset {

  @Property()
  public ID: string;

  @Property()
  public ObjectType: string;

  @Property()
  public OwnerOrg: string;

  @Property()
  public PublicDescription: string;

  constructor(assetID: string, clientOrgID: string, publicDescription: string) {
    this.ID = assetID;
    this.ObjectType = "asset";
    this.OwnerOrg = clientOrgID;
    this.PublicDescription = publicDescription;
  }

  public static Create(asset: IAsset) {
    return new Asset(asset.assetID, asset.clientOrgID, asset.publicDescription);
  }

  public serialize() : Uint8Array {
    return Buffer.from(this.toJSON());
  }

  public toJSON(): string {
    const assetDto: IAsset = {
      assetID: this.ID,
      clientOrgID: this.OwnerOrg,
      publicDescription: this.PublicDescription,
      objectType: this.ObjectType
    }
    return JSON.stringify(assetDto);
  }
}

export interface IAsset {
  assetID: string,
  clientOrgID: string,
  publicDescription: string,
  objectType?: string
}