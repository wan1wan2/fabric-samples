import { State } from './libs/State';
export declare class Asset extends State {
    docType?: string;
    Color: string;
    Size: number;
    Owner: string;
    AppraisedValue: number;
}
export declare class AssetDetail extends State {
    AppraisedValue: number;
    constructor(id: string, appraisedValue: number);
}
