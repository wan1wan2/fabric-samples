/*
  SPDX-License-Identifier: Apache-2.0
*/

import {Object, Property} from 'fabric-contract-api';
import { State } from './libs/State';

@Object()
export class Asset extends State {
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

}

@Object()
export class AssetDetail extends State {

    @Property()
    public AppraisedValue: number

    constructor(id: string, appraisedValue: number) {
      super();
      this.ID = id;
      this.AppraisedValue = appraisedValue;
    }
}
