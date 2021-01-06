package org.example.fabric.samples.securedagreement;

import org.bouncycastle.jcajce.provider.digest.SHA256;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ext.sbe.StateBasedEndorsement;
import org.hyperledger.fabric.shim.ext.sbe.impl.StateBasedEndorsementFactory;
import org.hyperledger.fabric.shim.ledger.CompositeKey;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Map;

@Contract(name="securedAgreement")
@Default
public class AssetTransfer implements ContractInterface {

    private final String typeAssetForSale = "S";
    private final String typeAssetBid = "B";
    private final String typeAssetSaleReceipt = "SR";
    private final String typeAssetBuyReceipt = "BR";

    private enum AssetTransferErrors {
        INCOMPLETE_INPUT,
        INVALID_ACCESS,
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS,
        PRICE_NOT_EQUAL,
        PROPERTY_NOT_EQUAL,
        UNKNOWN_ISSUE
    }

    /**
     * Get Asset By Asset ID
     * @param ctx Contract Context
     * @param assetID Asset ID
     * @return Asset Object
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Asset readAsset(final Context ctx, String assetID) {
        byte[] assetJSON = ctx.getStub().getState(assetID);
        if (assetJSON == null || assetJSON.length == 0) {
            String errorMessage = String.format("%s does not exist", assetID);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        return Asset.deserialize(assetJSON);
    }

    /**
     * Get Asset Private Data
     * @param ctx Contract Context
     * @param assetID Asset Id
     * @return Asset Private Data
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAssetPrivateProperties(final Context ctx, String assetID) {
        String collection = getClientImplicitCollectionName(ctx);
        byte[] immutableProperties = ctx.getStub().getPrivateData(collection, assetID);
        if (immutableProperties == null || immutableProperties.length == 0) {
            String errorMessage = String.format("asset private details does not exist in client org's collection: %s"
                    , assetID);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        return new String(immutableProperties, StandardCharsets.UTF_8);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAssetSalesPrice(final Context ctx, String assetID) {
        return getAssetPrice(ctx, assetID, typeAssetForSale);
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAssetBidPrice(final Context ctx, String assetID) {
        return getAssetPrice(ctx, assetID, typeAssetBid);
    }

    private String getAssetPrice(final Context ctx, String assetID, String priceType) {
        String collection = getClientImplicitCollectionName(ctx);
        CompositeKey assetPriceKey = ctx.getStub().createCompositeKey(priceType, assetID);
        byte[] price = ctx.getStub().getPrivateData(collection, assetPriceKey.toString());
        if (price == null || price.length == 0) {
            String errorMessage = String.format("asset price does not exist: %s", assetID);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }
        return new String(price, StandardCharsets.UTF_8);
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
     * Change Asset Publish Description
     * @param ctx Contract Context
     * @param assetID Asset Id
     * @param newDescription New publish description
     * @return Asset object with new publish description
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset changePublicDescription(final Context ctx, String assetID, String newDescription) {
        String clientOrgID = getClientOrgID(ctx, false);
        Asset asset = readAsset(ctx, assetID);

        if (!clientOrgID.equals(asset.getOwnerOrg())) {
            String errorMessage = String.format("a client from %s cannot update the description of a asset owned by %s"
                    , clientOrgID, asset.getOwnerOrg());
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.INVALID_ACCESS.toString());
        }

        asset.setPublicDescription(newDescription);
        ctx.getStub().putState(assetID, asset.serialize());
        return asset;
    }

    /**
     * AgreeToSell adds seller's asking price to seller's implicit private data collection
     * @param ctx Contract Context
     * @param assetID Asset id
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void agreeToSell(final Context ctx, String assetID) {
        Asset asset = readAsset(ctx, assetID);
        String clientOrgID = getClientOrgID(ctx, true);

        if (!clientOrgID.equals(asset.getOwnerOrg())) {
            String errorMessage = String.format("a client from %s cannot update the description of a asset owned by %s"
                    , clientOrgID, asset.getOwnerOrg());
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.INVALID_ACCESS.toString());
        }

        agreeToPrice(ctx, assetID, typeAssetForSale);
    }

    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void agreeToBuy(final Context ctx, String assetID) {
        readAsset(ctx, assetID);
        agreeToPrice(ctx, assetID, typeAssetBid);
    }

    /**
     * agreeToPrice adds a bid or ask price to caller's implicit private data collection
     * @param ctx Contract Context
     * @param assetID Asset id
     * @param priceType typeAssetForSale / typeAssetBid
     */
    private void agreeToPrice(final Context ctx, String assetID, String priceType) {
        Map<String, byte[]> transientMap = ctx.getStub().getTransient();
        if (!transientMap.containsKey("asset_price")) {
            String errorMessage = "asset_price key not found in the transient map";
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.INCOMPLETE_INPUT.toString());
        }
        byte[] price = transientMap.get("asset_price");
        String clientOrgID = getClientOrgID(ctx, true);
        String collection = buildCollectionName(clientOrgID);
        CompositeKey assetPriceKey = ctx.getStub().createCompositeKey(priceType, assetID);

        ctx.getStub().putPrivateData(collection, assetPriceKey.toString(), price);
    }

    /**
     * TransferAsset checks transfer conditions and then transfers asset state to buyer.
     * TransferAsset can only be called by current owner
     * @param ctx Contract Context
     * @param assetID Asset id
     * @param buyerOrgID buyer org id
     * @return asset after transfer owner
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset transferAsset(final Context ctx, String assetID, String buyerOrgID) {

        String clientOrgID = getClientOrgID(ctx, true);
        Map<String, byte[]> transientMap = ctx.getStub().getTransient();
        if (!transientMap.containsKey("asset_properties")) {
            String errorMessage = "asset_properties key not found in the transient map";
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.INCOMPLETE_INPUT.toString());
        } else if (!transientMap.containsKey("asset_price")) {
            String errorMessage = "asset_price key not found in the transient map";
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.INCOMPLETE_INPUT.toString());
        }

        byte[] immutablePropertiesJSON = transientMap.get("asset_properties");
        byte[] priceJSON = transientMap.get("asset_price");
        Asset asset = readAsset(ctx, assetID);
        Agreement agreement = Agreement.deserialize(priceJSON);

        verifyTransferConditions(ctx, asset, immutablePropertiesJSON, clientOrgID, buyerOrgID, priceJSON);

        transferAssetState(ctx, asset, immutablePropertiesJSON, clientOrgID, buyerOrgID, agreement.getPrice());

        return asset;
    }

    // verifyTransferConditions checks that client org currently owns asset and that both parties have agreed on price
    private void verifyTransferConditions(final Context ctx, Asset asset, byte[] immutablePropertiesJSON
            , String clientOrgID, String buyerOrgID, byte[] priceJSON) {

        // CHECK1: Auth check to ensure that client's org actually owns the asset
        if (!clientOrgID.equals(asset.getOwnerOrg())) {
            String errorMessage = String.format("a client from %s cannot transfer a asset owned by %s"
                    , clientOrgID, asset.getOwnerOrg());
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.INVALID_ACCESS.toString());
        }

        // CHECK2: Verify that the hash of the passed immutable properties matches the on-chain hash
        String collectionSeller = buildCollectionName(clientOrgID);
        byte[] immutablePropertiesOnChainHash = ctx.getStub().getPrivateDataHash(collectionSeller, asset.getAssetID());
        if (immutablePropertiesOnChainHash == null || immutablePropertiesOnChainHash.length == 0) {
            String errorMessage = String.format("asset private properties hash does not exist: %s", asset.getAssetID());
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new ChaincodeException(e.getMessage(), AssetTransferErrors.UNKNOWN_ISSUE.toString());
        }
        byte[] calculatedPropertiesHash = digest.digest(immutablePropertiesJSON);

        if (!Arrays.equals(calculatedPropertiesHash, immutablePropertiesOnChainHash)) {
            String errorMessage = String.format("hash %s for passed immutable properties %s does not match on-chain hash %s",
                    new String(calculatedPropertiesHash, StandardCharsets.UTF_8),
                    new String(immutablePropertiesJSON, StandardCharsets.UTF_8),
                    new String(immutablePropertiesOnChainHash, StandardCharsets.UTF_8));
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.PROPERTY_NOT_EQUAL.toString());
        }

        // CHECK3: Verify that seller and buyer agreed on the same price

        String assetForSaleKey = ctx.getStub().createCompositeKey(typeAssetForSale, asset.getAssetID())
                .toString();
        byte[] sellerPriceHash = ctx.getStub().getPrivateDataHash(collectionSeller, assetForSaleKey);
        if (sellerPriceHash == null || sellerPriceHash.length == 0) {
            String errorMessage = String.format("seller price for %s does not exist", asset.getAssetID());
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        // Get buyers bid price
        String collectionBuyer = buildCollectionName(buyerOrgID);
        String assetBidKey = ctx.getStub().createCompositeKey(typeAssetBid, asset.getAssetID()).toString();
        byte[] buyerPriceHash = ctx.getStub().getPrivateDataHash(collectionBuyer, assetBidKey);
        if (buyerPriceHash == null || buyerPriceHash.length == 0) {
            String errorMessage = String.format("buyer price for %s does not exist", asset.getAssetID());
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        byte[] calculatedPriceHash = digest.digest(priceJSON);
        if (!Arrays.equals(calculatedPriceHash, sellerPriceHash)) {
            String errorMessage = String.format("hash %s for passed price JSON %s does not match on-chain hash %s" +
                            ", seller hasn't agreed to the passed trade id and price",
                    new String(calculatedPriceHash, StandardCharsets.UTF_8),
                    new String(priceJSON, StandardCharsets.UTF_8),
                    new String(sellerPriceHash, StandardCharsets.UTF_8));
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.PRICE_NOT_EQUAL.toString());
        }

        if (!Arrays.equals(calculatedPriceHash, buyerPriceHash)) {
            String errorMessage = String.format("hash %s for passed price JSON %s does not match on-chain hash %s" +
                            ", buyer hasn't agreed to the passed trade id and price",
                    new String(calculatedPriceHash, StandardCharsets.UTF_8),
                    new String(priceJSON, StandardCharsets.UTF_8),
                    new String(sellerPriceHash, StandardCharsets.UTF_8));
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.PRICE_NOT_EQUAL.toString());
        }

    }

    private void transferAssetState(final Context ctx, Asset asset, byte[] immutablePropertiesJSON
            , String clientOrgID, String buyerOrgID, int price) {
        asset.setOwnerOrg(buyerOrgID);
        ctx.getStub().putState(asset.getAssetID(), asset.serialize());

        // Change the endorsement policy to the new owner
        setAssetStateBasedEndorsement(ctx, asset.getAssetID(), buyerOrgID);

        String collectionSeller = buildCollectionName(clientOrgID);
        ctx.getStub().delPrivateData(collectionSeller, asset.getAssetID());

        String collectionBuyer = buildCollectionName(buyerOrgID);
        ctx.getStub().putPrivateData(collectionBuyer, asset.getAssetID(), immutablePropertiesJSON);

        // Delete the price records for seller
        String assetPriceKey = ctx.getStub().createCompositeKey(typeAssetForSale, asset.getAssetID()).toString();
        ctx.getStub().delPrivateData(collectionSeller, assetPriceKey);

        // Delete the price records for buyer
        assetPriceKey = ctx.getStub().createCompositeKey(typeAssetBid, asset.getAssetID()).toString();
        ctx.getStub().delPrivateData(collectionBuyer, assetPriceKey);

        // Keep record for a 'receipt' in both buyers and sellers private data collection to record the sale price and date.
        // Persist the agreed to price in a collection sub-namespace based on receipt key prefix.
        String receiptBuyKey = ctx.getStub()
                .createCompositeKey(typeAssetBuyReceipt, asset.getAssetID(), ctx.getStub().getTxId())
                .toString();
        long timestamp = ctx.getStub().getTxTimestamp().getEpochSecond();

        AssetReceipt assetReceipt = new AssetReceipt(asset.getAssetID(), price, timestamp);
        ctx.getStub().putPrivateData(collectionBuyer, receiptBuyKey, assetReceipt.serialize());

        String receiptSellerKey = ctx.getStub()
                .createCompositeKey(typeAssetSaleReceipt, asset.getAssetID(), ctx.getStub().getTxId())
                .toString();
        ctx.getStub().putPrivateData(collectionSeller, receiptSellerKey, assetReceipt.serialize());

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

    private String getClientImplicitCollectionName(Context ctx) {
        String clientOrgID = getClientOrgID(ctx, true);
        verifyClientOrgMatchesPeerOrg(ctx, clientOrgID);
        return buildCollectionName(clientOrgID);
    }

    private String buildCollectionName(String clientOrgID) {
        return String.format("_implicit_org_%s", clientOrgID);
    }
}
