package org.example.fabric.samples.ledger;

import org.example.fabric.samples.ledger.models.Asset;
import org.example.fabric.samples.ledger.models.HistoryQueryResult;
import org.example.fabric.samples.ledger.models.PaginatedQueryResult;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.protos.peer.ChaincodeShim;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.shim.ledger.CompositeKey;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AssetTransferTest {

    @Nested
    class InvokeTransferTest {

        @Test
        public void createAssetSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Asset testAsset = getTestAsset();
            CompositeKey ck = mock(CompositeKey.class);
            when(ck.toString())
                    .thenReturn(String.format("%s%s%s", indexKey, testAsset.getColor(), testAsset.getAssetID()));
            when(stub.createCompositeKey(indexKey, testAsset.getColor(), testAsset.getAssetID()))
                    .thenReturn(ck);

            Asset asset = contract.createAsset(ctx, testAsset.getAssetID(), testAsset.getColor(),
                    testAsset.getSize(), testAsset.getOwner(), testAsset.getAppraisedValue());
            assertThat(asset).isNotNull();
            assertThat(asset).isEqualTo(testAsset);

        }

        @Test
        public void createAssetReturnAssetExists() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Asset asset = getTestAsset();
            when(stub.getState(asset.getAssetID())).thenReturn(asset.serialize());

            Throwable thrown = catchThrowable(() ->
                contract.createAsset(ctx, "asset1", "red", 16, "Org1MSP", 200)
            );

            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause();
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_ALREADY_EXISTS".getBytes());
        }

        @Test
        public void deleteAssetSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Asset testAsset = getTestAsset();
            when(stub.getState(testAsset.getAssetID())).thenReturn(testAsset.serialize());
            CompositeKey ck = mock(CompositeKey.class);
            when(ck.toString())
                    .thenReturn(String.format("%s%s%s", indexKey, testAsset.getColor(), testAsset.getAssetID()));
            when(stub.createCompositeKey(indexKey, testAsset.getColor(), testAsset.getAssetID()))
                    .thenReturn(ck);

            Throwable thrown = catchThrowable(() -> contract.deleteAsset(ctx, testAsset.getAssetID()));
            assertThat(thrown).isNull();
        }

        @Test
        public void deleteAssetReturnAssetNotFound() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Asset testAsset = getTestAsset();
            when(stub.getState(testAsset.getAssetID())).thenReturn(testAsset.serialize());

            Throwable thrown = catchThrowable(() -> contract.deleteAsset(ctx, "asset2"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause();
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
        }

        @Test
        public void transferAssetSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Asset testAsset = getTestAsset();
            when(stub.getState(testAsset.getAssetID())).thenReturn(testAsset.serialize());

            String newOwner = "new owner";
            Asset asset = contract.transferAsset(ctx, testAsset.getAssetID(), newOwner);
            assertThat(asset).isNotNull();
            assertThat(asset.getOwner()).isEqualTo(newOwner);
            testAsset.setOwner(newOwner);
            assertThat(asset).isEqualTo(testAsset);
        }

        @Test
        public void transferAssetByColorSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStateByPartialCompositeKey(indexKey, "red"))
                    .thenReturn(new MockAssetResultsIterator());
            Asset testAsset = getTestAsset();
            CompositeKey ck = mock(CompositeKey.class);
            when(ck.toString()).thenReturn(testAsset.getAssetID());
            when(stub.getState(testAsset.getAssetID())).thenReturn(testAsset.serialize());
            when(stub.splitCompositeKey(testAsset.getAssetID())).thenReturn(ck);

            contract.transferAssetByColor(ctx, testAsset.getColor(), "new owner");

        }
    }

    @Nested
    class QueryTest {

        @Test
        public void assetExistTest() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Asset asset = getTestAsset();
            when(stub.getState(asset.getAssetID())).thenReturn(asset.serialize());

            Boolean isExists = contract.assetExists(ctx, asset.getAssetID());
            assertThat(isExists).isEqualTo(true);
            isExists = contract.assetExists(ctx, "whatever");
            assertThat(isExists).isEqualTo(false);
        }

        @Test
        public void readAssetSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Asset testAsset = getTestAsset();
            when(stub.getState(testAsset.getAssetID())).thenReturn(testAsset.serialize());

            Asset asset = contract.readAsset(ctx, testAsset.getAssetID());
            assertThat(asset).isNotNull();
            assertThat(asset).isEqualTo(testAsset);
        }

        @Test
        public void readAssetReturnAssetNotFound() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Asset testAsset = getTestAsset();
            when(stub.getState(testAsset.getAssetID())).thenReturn(testAsset.serialize());

            Throwable thrown = catchThrowable(() -> contract.readAsset(ctx, "asset2"));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause();
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("ASSET_NOT_FOUND".getBytes());
        }

        @Test
        public void getAssetByRangeSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStateByRange("", "")).thenReturn(new MockAssetResultsIterator());

            List<Asset> assets = contract.getAssetsByRange(ctx, "", "");
            assertThat(assets).isNotNull();
            assertThat(assets.size()).isEqualTo(1);
        }

        @Test
        public void getAssetByRangePaginationSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getStateByRangeWithPagination("", "", 10, ""))
                    .thenReturn(new MockAssetResultsIteratorWithMetadata());

            List<Asset> assets = contract.getAssetsByRangeWithPagination(ctx, "", "", 10, "");
            assertThat(assets).isNotNull();
            assertThat(assets.size()).isEqualTo(1);
        }

        @Test
        public void queryAssetByOwnerSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Asset testAsset = getTestAsset();
            String queryString = String.format("{\"selector\":{\"docType\":\"asset\",\"owner\":\"%s\"}}",
                    testAsset.getOwner());
            when(stub.getQueryResult(queryString)).thenReturn(new MockAssetResultsIterator());

            List<Asset> assets = contract.queryAssetsByOwner(ctx, testAsset.getOwner());
            assertThat(assets).isNotNull();
            assertThat(assets.size()).isEqualTo(1);
        }

        @Test
        public void queryAssetSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Asset testAsset = getTestAsset();
            String queryString = String.format("{\"selector\":{\"docType\":\"asset\",\"owner\":\"%s\"}}",
                    testAsset.getOwner());
            when(stub.getQueryResult(queryString)).thenReturn(new MockAssetResultsIterator());

            List<Asset> assets = contract.queryAssets(ctx, queryString);
            assertThat(assets).isNotNull();
            assertThat(assets.size()).isEqualTo(1);
        }

        @Test
        public void queryAssetsWithPaginationSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Asset testAsset = getTestAsset();
            String queryString = String.format("{\"selector\":{\"docType\":\"asset\",\"owner\":\"%s\"}}",
                    testAsset.getOwner());
            int pageSize = 10;
            String bookmark = "";
            when(stub.getQueryResultWithPagination(queryString, pageSize, bookmark))
                    .thenReturn(new MockAssetResultsIteratorWithMetadata());

            PaginatedQueryResult result = contract.queryAssetsWithPagination(ctx, queryString, pageSize, bookmark);
            assertThat(result).isNotNull();
            assertThat(result.getFetchedRecordsCount()).isEqualTo(1);
            assertThat(result.getBookmark()).isEqualTo(bookmark);
            assertThat(result.getRecords().size()).isEqualTo(1);
        }

        @Test
        public void getAssetHistorySuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            Asset testAsset = getTestAsset();
            when(stub.getHistoryForKey(testAsset.getAssetID()))
                    .thenReturn(new MockAssetHistoryResultsIterator());

            List<HistoryQueryResult> results = contract.getAssetHistory(ctx, testAsset.getAssetID());
            assertThat(results).isNotNull();
            assertThat(results.size() > 0).isTrue();
        }

    }

    private final String testOrg1MSP = "Org1MSP";
    private final String indexKey = "color~name";

    private Asset getTestAsset() {
        return new Asset("asset1", "asset", "red", 16, testOrg1MSP, 200);
    }

    private final class MockKeyValue implements KeyValue {

        private final String key;
        private final String value;

        MockKeyValue(final String key, final String value) {
            super();
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return this.key;
        }

        @Override
        public String getStringValue() {
            return this.value;
        }

        @Override
        public byte[] getValue() {
            return this.value.getBytes();
        }

    }

    private final class MockKeyModification implements KeyModification {

        private final String txId;
        private final byte[] value;
        private final Instant timestamp;

        MockKeyModification(final byte[] bytes) {
            this.txId = "txId";
            this.value = bytes;
            this.timestamp = Instant.now();
        }

        @Override
        public String getTxId() {
            return this.txId;
        }

        @Override
        public byte[] getValue() {
            return this.value;
        }

        @Override
        public String getStringValue() {
            return new String(this.value, StandardCharsets.UTF_8);
        }

        @Override
        public Instant getTimestamp() {
            return this.timestamp;
        }

        @Override
        public boolean isDeleted() {
            return false;
        }
    }


    private final class MockAssetResultsIterator implements QueryResultsIterator<KeyValue> {

        private final List<KeyValue> assetList;

        MockAssetResultsIterator() {
            super();

            Asset[] assets = {
                    getTestAsset()
            };
            assetList = new ArrayList<KeyValue>();
            for (Asset asset: assets) {
                assetList.add(new MockKeyValue(asset.getAssetID(), asset.toJSON()));
            }
        }

        @Override
        public Iterator<KeyValue> iterator() {
            return assetList.iterator();
        }

        @Override
        public void close() throws Exception {
            // do nothing
        }
    }

    private final class MockAssetHistoryResultsIterator implements QueryResultsIterator<KeyModification> {

        private final List<KeyModification> assetHistoryList;

        MockAssetHistoryResultsIterator() {
            super();
            assetHistoryList = new ArrayList<KeyModification>();
            for (int i = 0; i < 5; i++) {
                Asset asset = getTestAsset();
                assetHistoryList.add(new MockKeyModification(asset.serialize()));
            }
        }

        @Override
        public Iterator<KeyModification> iterator() {
            return assetHistoryList.iterator();
        }

        @Override
        public void close() throws Exception {
            // do nothing
        }
    }

    private final class MockAssetResultsIteratorWithMetadata implements QueryResultsIteratorWithMetadata<KeyValue> {

        private final List<KeyValue> assetList;

        MockAssetResultsIteratorWithMetadata() {
            super();

            Asset[] assets = {
                    getTestAsset()
            };
            assetList = new ArrayList<KeyValue>();
            for (Asset asset: assets) {
                assetList.add(new MockKeyValue(asset.getAssetID(), asset.toJSON()));
            }
        }

        @Override
        public Iterator<KeyValue> iterator() {
            return assetList.iterator();
        }

        @Override
        public void close() throws Exception {
            // do nothing
        }

        @Override
        public ChaincodeShim.QueryResponseMetadata getMetadata() {
            return ChaincodeShim.QueryResponseMetadata
                    .newBuilder()
                    .setFetchedRecordsCount(assetList.size())
                    .build();
        }
    }

}
