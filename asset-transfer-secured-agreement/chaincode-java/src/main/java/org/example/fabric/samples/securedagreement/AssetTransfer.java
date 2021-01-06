package org.example.fabric.samples.securedagreement;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;
import org.hyperledger.fabric.shim.ext.sbe.impl.StateBasedEndorsementFactory;

import java.util.Map;

@Contract(name="securedAgreement")
@Default
public class AssetTransfer implements ContractInterface {

    private enum AssetTransferErrors {
        INCOMPLETE_INPUT,
        INVALID_ACCESS,
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS
    }

    /**
     * create asset
     * @param ctx contact context
     * @param assetID input asset Id
     * @param publicDescription input public description text
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset createAsset(final Context ctx, final String assetID, final String publicDescription) {
        Map<String, byte[]> transientMap = ctx.getStub().getTransient();
        if (!transientMap.containsKey("asset_properties")) {
            String errorMessage = "CreateAsset call must specify asset_properties in Transient map input";
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.INCOMPLETE_INPUT.toString());
        }

        byte[] immutablePropertiesJSON = transientMap.get("asset_properties");
        String clientOrgID = getClientOrgID(ctx, true);
        Asset asset = new Asset(assetID, clientOrgID, publicDescription);
        ctx.getStub().putState(assetID, asset.serialize());

        // Set the endorsement policy such that an owner org peer is required to endorse future updates
        setAssetStateBasedEndorsement(ctx, assetID, clientOrgID);

        String collectionName = buildCollectionName(clientOrgID);
        ctx.getStub().putPrivateData(collectionName, assetID, immutablePropertiesJSON);

        return asset;
    }

    /**
     * get client msp id from client
     * @param ctx contract context
     * @param verifyOrg need to verify client id
     * @return client msp id
     */
    private String getClientOrgID(final Context ctx, boolean verifyOrg) {
        String clientOrgID = ctx.getClientIdentity().getMSPID();

        if (verifyOrg) {
            verifyClientOrgMatchesPeerOrg(ctx, clientOrgID);
        }

        return clientOrgID;
    }

    /**
     *
     * @param ctx contract context
     * @param clientOrgID msp id from client identity
     */
    private void verifyClientOrgMatchesPeerOrg(final Context ctx, String clientOrgID) {
        String peerMSPID = ctx.getStub().getMspId();
        if (!clientOrgID.equals(peerMSPID)) {
            String errorMessage = String.format("Client from org %s is not authorized to read or write private data from an org %s peer"
                    , clientOrgID, peerMSPID);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.INVALID_ACCESS.toString());
        }
    }

    private void setAssetStateBasedEndorsement(final Context ctx, String assetID, String orgToEndorse) {
        StateBasedEndorsement endorsementPolicy = StateBasedEndorsementFactory.getInstance()
                .newStateBasedEndorsement(null);
        endorsementPolicy.addOrgs(StateBasedEndorsement.RoleType.RoleTypePeer, orgToEndorse);
        byte[] policy = endorsementPolicy.policy();
        ctx.getStub().setStateValidationParameter(assetID, policy);
    }

    private String buildCollectionName(String clientOrgID) {
        return String.format("_implicit_org_%s", clientOrgID);
    }
}
