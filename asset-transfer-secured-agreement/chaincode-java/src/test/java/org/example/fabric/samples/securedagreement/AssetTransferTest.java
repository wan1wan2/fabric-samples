package org.example.fabric.samples.securedagreement;

import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AssetTransferTest {
    @Nested
    class InvokeWriteTransaction {

        @Test
        public void createAssetWithoutTransientMap() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Map<String, byte[]> transientMap = new HashMap<>();
            when(stub.getTransient()).thenReturn(transientMap);

            Throwable thrown = catchThrowable(()-> contract.createAsset(ctx, "asset1", "public description text"));

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause();
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("INCOMPLETE_INPUT".getBytes());
        }

        @Test
        public void createAssetWithDifferentOrg() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getMspId()).thenReturn("whatever");
            ClientIdentity ci = mock(ClientIdentity.class);
            when(ci.getId()).thenReturn(testOrg1Client);
            when(ci.getMSPID()).thenReturn(testOrgOneMSP);
            when(ctx.getClientIdentity()).thenReturn(ci);

            Map<String, byte[]> transientMap = new HashMap<>();
            AssetProperty assetProperty = new AssetProperty("asset1", "asset_properties"
                    , "blue", 16, "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3");
            transientMap.put("asset_properties", assetProperty.serialize());
            when(stub.getTransient()).thenReturn(transientMap);

            Throwable thrown = catchThrowable(()-> contract.createAsset(ctx, "asset1", "public description text"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause();
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("INVALID_ACCESS".getBytes());
        }

        @Test
        public void createAssetShouldBeSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getMspId()).thenReturn(testOrgOneMSP);
            ClientIdentity ci = mock(ClientIdentity.class);
            when(ci.getId()).thenReturn(testOrg1Client);
            when(ci.getMSPID()).thenReturn(testOrgOneMSP);
            when(ctx.getClientIdentity()).thenReturn(ci);

            Map<String, byte[]> transientMap = new HashMap<>();
            AssetProperty assetProperty = new AssetProperty("asset1", "asset_properties"
                    , "blue", 16, "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3");
            transientMap.put("asset_properties", assetProperty.serialize());
            when(stub.getTransient()).thenReturn(transientMap);

            Asset asset = contract.createAsset(ctx, "asset1", "public description text");
            assertThat(asset).isNotNull();
            assertThat(asset).isEqualTo(getTestAsset());
        }

        @Test
        public void changePublishDescriptionByAnotherOrg() {
            AssetTransfer contract = new AssetTransfer();
            Asset testAsset = getTestAsset();

            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getMspId()).thenReturn(testOrgOneMSP);
            when(stub.getState("asset1")).thenReturn(testAsset.serialize());
            ClientIdentity ci = mock(ClientIdentity.class);
            when(ci.getId()).thenReturn(testOrg1Client);
            when(ci.getMSPID()).thenReturn("another org");
            when(ctx.getClientIdentity()).thenReturn(ci);

            Throwable thrown = catchThrowable(()->
                    contract.changePublicDescription(ctx, "asset1", "new publish description"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause();
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("INVALID_ACCESS".getBytes());

        }

        @Test
        public void changePublishDescriptionShouldBeSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Asset testAsset = getTestAsset();

            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getMspId()).thenReturn(testOrgOneMSP);
            when(stub.getState("asset1")).thenReturn(testAsset.serialize());
            ClientIdentity ci = mock(ClientIdentity.class);
            when(ci.getId()).thenReturn(testOrg1Client);
            when(ci.getMSPID()).thenReturn(testOrgOneMSP);
            when(ctx.getClientIdentity()).thenReturn(ci);

            String newPublishDescription = "new publish description";
            Asset asset = contract.changePublicDescription(ctx, "asset1", newPublishDescription);
            assertThat(asset.getPublicDescription()).isEqualTo(newPublishDescription);
        }

        @Test
        public void agreeToSellSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Asset testAsset = getTestAsset();

            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getMspId()).thenReturn(testOrgOneMSP);
            when(stub.getState("asset1")).thenReturn(testAsset.serialize());
            CompositeKey ck = mock(CompositeKey.class);
            when(ck.toString()).thenReturn(typeAssetForSale + "asset1");
            when(stub.createCompositeKey(typeAssetForSale, "asset1")).thenReturn(ck);
            ClientIdentity ci = mock(ClientIdentity.class);
            when(ci.getId()).thenReturn(testOrg1Client);
            when(ci.getMSPID()).thenReturn(testOrgOneMSP);
            when(ctx.getClientIdentity()).thenReturn(ci);

            Map<String, byte[]> transientMap = new HashMap<>();
            transientMap.put("asset_price", "asset_price".getBytes(StandardCharsets.UTF_8));
            when(stub.getTransient()).thenReturn(transientMap);

            contract.agreeToSell(ctx, "asset1");
        }

        @Test
        public void agreeToBuySuccess() {
            AssetTransfer contract = new AssetTransfer();
            Asset testAsset = getTestAsset();

            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getMspId()).thenReturn(testOrgOneMSP);
            when(stub.getState("asset1")).thenReturn(testAsset.serialize());
            CompositeKey ck = mock(CompositeKey.class);
            when(ck.toString()).thenReturn(typeAssetBid + "asset1");
            when(stub.createCompositeKey(typeAssetBid, "asset1")).thenReturn(ck);
            ClientIdentity ci = mock(ClientIdentity.class);
            when(ci.getId()).thenReturn(testOrg1Client);
            when(ci.getMSPID()).thenReturn(testOrgOneMSP);
            when(ctx.getClientIdentity()).thenReturn(ci);

            Map<String, byte[]> transientMap = new HashMap<>();
            transientMap.put("asset_price", "asset_price".getBytes(StandardCharsets.UTF_8));
            when(stub.getTransient()).thenReturn(transientMap);

            contract.agreeToBuy(ctx, "asset1");
        }

        @Test
        public void transferAssetSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Asset testAsset = getTestAsset();

            Context ctx = mock(Context.class);
            ChaincodeStub stub = prepareTransferAssetTest(ctx, testAsset);
            assertThat(stub).isNotNull();

            Agreement agreement = getTestAgreement();
            AssetProperty assetProperty = getTestAssetProperty();
            Map<String, byte[]> transientMap = new HashMap<>();
            transientMap.put("asset_price", agreement.serialize());
            transientMap.put("asset_properties", assetProperty.serialize());
            when(stub.getTransient()).thenReturn(transientMap);

            Asset asset = contract.transferAsset(ctx, "asset1", testOrgTwoMSP);
            assertThat(asset.getOwnerOrg()).isEqualTo(testOrgTwoMSP);
        }

        @Test
        public void transferAssetReturnPriceError() {
            AssetTransfer contract = new AssetTransfer();
            Asset testAsset = getTestAsset();

            Context ctx = mock(Context.class);
            ChaincodeStub stub = prepareTransferAssetTest(ctx, testAsset);
            assertThat(stub).isNotNull();

            Agreement agreement = getTestAgreement2();
            AssetProperty assetProperty = getTestAssetProperty();
            Map<String, byte[]> transientMap = new HashMap<>();
            transientMap.put("asset_price", agreement.serialize());
            transientMap.put("asset_properties", assetProperty.serialize());
            when(stub.getTransient()).thenReturn(transientMap);

            Throwable thrown = catchThrowable(()->
                    contract.transferAsset(ctx, "asset1", testOrgTwoMSP) );
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause();
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("PRICE_NOT_EQUAL".getBytes());
        }

        @Test
        public void transferAssetReturnPropertyError() {
            AssetTransfer contract = new AssetTransfer();
            Asset testAsset = getTestAsset();

            Context ctx = mock(Context.class);
            ChaincodeStub stub = prepareTransferAssetTest(ctx, testAsset);
            assertThat(stub).isNotNull();

            Agreement agreement = getTestAgreement();
            AssetProperty assetProperty = getTestAssetProperty2();
            Map<String, byte[]> transientMap = new HashMap<>();
            transientMap.put("asset_price", agreement.serialize());
            transientMap.put("asset_properties", assetProperty.serialize());
            when(stub.getTransient()).thenReturn(transientMap);

            Throwable thrown = catchThrowable(()->
                    contract.transferAsset(ctx, "asset1", testOrgTwoMSP) );
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause();
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("PROPERTY_NOT_EQUAL".getBytes());
        }

        @Test
        public void transferAssetReturnBuyerError() {
            AssetTransfer contract = new AssetTransfer();
            Asset testAsset = getTestAsset();

            Context ctx = mock(Context.class);
            ChaincodeStub stub = prepareTransferAssetTest(ctx, testAsset);
            assertThat(stub).isNotNull();

            Agreement agreement = getTestAgreement();
            AssetProperty assetProperty = getTestAssetProperty();
            Map<String, byte[]> transientMap = new HashMap<>();
            transientMap.put("asset_price", agreement.serialize());
            transientMap.put("asset_properties", assetProperty.serialize());
            when(stub.getTransient()).thenReturn(transientMap);

            Throwable thrown = catchThrowable(()->
                    contract.transferAsset(ctx, "asset1", testOrgThreeMSP) );
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause();
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
        }

        private ChaincodeStub prepareTransferAssetTest(Context ctx, Asset asset) {

            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getMspId()).thenReturn(testOrgOneMSP);
            when(stub.getState("asset1")).thenReturn(asset.serialize());
            when(stub.getTxId()).thenReturn("txtId");
            Instant instant = Instant.now();
            when(stub.getTxTimestamp()).thenReturn(instant);

            // prepare composite key
            CompositeKey ckForSale = mock(CompositeKey.class);
            when(ckForSale.toString()).thenReturn(typeAssetForSale + "asset1");
            when(stub.createCompositeKey(typeAssetForSale, "asset1")).thenReturn(ckForSale);
            CompositeKey ckForBuyer = mock(CompositeKey.class);
            when(ckForBuyer.toString()).thenReturn(typeAssetBid + "asset1");
            when(stub.createCompositeKey(typeAssetBid, "asset1")).thenReturn(ckForBuyer);

            CompositeKey ckFroSaleReceipt = mock(CompositeKey.class);
            when(ckFroSaleReceipt.toString()).thenReturn(typeAssetSaleReceipt + "asset1");
            when(stub.createCompositeKey(typeAssetSaleReceipt, "asset1", stub.getTxId()))
                    .thenReturn(ckFroSaleReceipt);
            CompositeKey ckFroBuyerReceipt = mock(CompositeKey.class);
            when(ckFroBuyerReceipt.toString()).thenReturn(typeAssetBuyReceipt + "asset1");
            when(stub.createCompositeKey(typeAssetBuyReceipt, "asset1", stub.getTxId()))
                    .thenReturn(ckFroBuyerReceipt);

            // prepare client identity
            ClientIdentity ci = mock(ClientIdentity.class);
            when(ci.getId()).thenReturn(testOrg1Client);
            when(ci.getMSPID()).thenReturn(testOrgOneMSP);
            when(ctx.getClientIdentity()).thenReturn(ci);

            // prepare transient
            Agreement agreement = getTestAgreement();
            AssetProperty assetProperty = getTestAssetProperty();

            // prepare get private hash
            MessageDigest digest;
            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                return null;
            }
            String collectionSeller = buildCollectionName(testOrgOneMSP);
            String collectionBuyer = buildCollectionName(testOrgTwoMSP);

            byte[] hashedProperty = digest.digest(assetProperty.serialize());
            byte[] hashedAgreement = digest.digest(agreement.serialize());
            when(stub.getPrivateDataHash(collectionSeller, asset.getAssetID()))
                    .thenReturn(hashedProperty);
            String assetForSaleKey = ctx.getStub().createCompositeKey(typeAssetForSale, asset.getAssetID())
                    .toString();
            String assetForBuyerKey = ctx.getStub().createCompositeKey(typeAssetBid, asset.getAssetID())
                    .toString();
            when(stub.getPrivateDataHash(collectionSeller, assetForSaleKey))
                    .thenReturn(hashedAgreement);
            when(stub.getPrivateDataHash(collectionBuyer, assetForBuyerKey))
                    .thenReturn(hashedAgreement);

            return stub;
        }

    }

    @Nested
    class QueryTransaction {


        @Test
        public void getAssetPrivatePropertiesShouldBeSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getMspId()).thenReturn(testOrgOneMSP);
            ClientIdentity ci = mock(ClientIdentity.class);
            when(ci.getId()).thenReturn(testOrg1Client);
            when(ci.getMSPID()).thenReturn(testOrgOneMSP);
            when(ctx.getClientIdentity()).thenReturn(ci);

            AssetProperty assetProperty = new AssetProperty("asset1", "asset_properties"
                    , "blue", 16, "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3");
            when(stub.getPrivateData("_implicit_org_"+testOrgOneMSP, "asset1"))
                    .thenReturn(assetProperty.serialize());
            String assetForSaleString = contract.getAssetPrivateProperties(ctx, "asset1");
            assertThat(assetForSaleString).isNotNull();
        }

        @Test
        public void readNotExistAsset() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Asset testAsset = getTestAsset();
            when(stub.getState("asset1")).thenReturn(testAsset.serialize());

            Throwable thrown = catchThrowable(()-> contract.readAsset(ctx, "asset2"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause();
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
        }

        @Test
        public void readAssetShouldBeSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Asset testAsset = getTestAsset();
            when(stub.getState("asset1")).thenReturn(testAsset.serialize());

            Asset asset = contract.readAsset(ctx, "asset1");
            assertThat(asset).isEqualTo(testAsset);
        }

        @Test
        public void getAssetSalesPriceSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getMspId()).thenReturn(testOrgOneMSP);
            CompositeKey ck = mock(CompositeKey.class);
            when(ck.toString()).thenReturn(typeAssetForSale + "asset1");
            when(stub.createCompositeKey(typeAssetForSale, "asset1")).thenReturn(ck);

            ClientIdentity ci = mock(ClientIdentity.class);
            when(ci.getId()).thenReturn(testOrg1Client);
            when(ci.getMSPID()).thenReturn(testOrgOneMSP);
            when(ctx.getClientIdentity()).thenReturn(ci);

            String testPriceContent = "price content";
            String collection = String.format("_implicit_org_%s", testOrgOneMSP);
            when(stub.getPrivateData(collection, typeAssetForSale + "asset1"))
                    .thenReturn(testPriceContent.getBytes(StandardCharsets.UTF_8));

            String testResult = contract.getAssetSalesPrice(ctx, "asset1");
            assertThat(testResult).isEqualTo(testPriceContent);
        }

        @Test
        public void getAssetBidPriceSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getMspId()).thenReturn(testOrgOneMSP);
            CompositeKey ck = mock(CompositeKey.class);
            when(ck.toString()).thenReturn(typeAssetBid + "asset1");
            when(stub.createCompositeKey(typeAssetBid, "asset1")).thenReturn(ck);

            ClientIdentity ci = mock(ClientIdentity.class);
            when(ci.getId()).thenReturn(testOrg1Client);
            when(ci.getMSPID()).thenReturn(testOrgOneMSP);
            when(ctx.getClientIdentity()).thenReturn(ci);

            String testPriceContent = "price content";
            String collection = String.format("_implicit_org_%s", testOrgOneMSP);
            when(stub.getPrivateData(collection, typeAssetBid + "asset1"))
                    .thenReturn(testPriceContent.getBytes(StandardCharsets.UTF_8));

            String testResult = contract.getAssetBidPrice(ctx, "asset1");
            assertThat(testResult).isEqualTo(testPriceContent);
        }

    }

    private Asset getTestAsset() {
        return new Asset("asset1", testOrgOneMSP, "public description text");
    }

    private Agreement getTestAgreement() {
        return new Agreement("asset1", 100, "test trade id");
    }

    private Agreement getTestAgreement2() {
        return new Agreement("asset1", 200, "test trade id");
    }

    private AssetProperty getTestAssetProperty() {
        return new AssetProperty("asset1", "asset_properties"
                , "red", 16, "test salt");
    }

    private AssetProperty getTestAssetProperty2() {
        return new AssetProperty("asset1", "asset_properties"
                , "blue", 16, "test salt");
    }

    private String buildCollectionName(String clientOrgID) {
        return String.format("_implicit_org_%s", clientOrgID);
    }

    private static final String testOrg1Client = "testOrg1User";
    private static final String testOrgOneMSP = "TestOrg1";
    private static final String testOrg2Client = "testOrg2User";
    private static final String testOrgTwoMSP = "TestOrg2";
    private static final String testOrgThreeMSP = "TestOrg3";
    private final String typeAssetForSale = "S";
    private final String typeAssetBid = "B";
    private final String typeAssetSaleReceipt = "SR";
    private final String typeAssetBuyReceipt = "BR";
}
