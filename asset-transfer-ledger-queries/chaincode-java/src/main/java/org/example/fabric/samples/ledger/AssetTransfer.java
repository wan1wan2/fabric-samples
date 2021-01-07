package org.example.fabric.samples.ledger;

import org.example.fabric.samples.ledger.models.Asset;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;

@Contract(name = "ledger")
@Default
public final class AssetTransfer implements ContractInterface {

    private enum AssetTransferErrors {
        INCOMPLETE_INPUT,
        INVALID_ACCESS,
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS
    }

    public Asset CreateAsset(final Context ctx, final String assetID, final String color,
                             final int size, final String owner, final int appraisedValue) {

        return null;
    }
}