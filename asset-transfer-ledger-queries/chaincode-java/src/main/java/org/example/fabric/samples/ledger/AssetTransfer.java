package org.example.fabric.samples.ledger;

import org.example.fabric.samples.ledger.models.Asset;
import org.example.fabric.samples.ledger.models.HistoryQueryResult;
import org.example.fabric.samples.ledger.models.PaginatedQueryResult;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.hyperledger.fabric.shim.ledger.QueryResultsIteratorWithMetadata;

import java.util.ArrayList;
import java.util.List;
import com.owlike.genson.Genson;

@Contract(name = "ledger")
@Default
public final class AssetTransfer implements ContractInterface {

    private final String index = "color~name";
    private final Genson genson = new Genson();

    private enum AssetTransferErrors {
        INCOMPLETE_INPUT,
        INVALID_ACCESS,
        ASSET_NOT_FOUND,
        ASSET_ALREADY_EXISTS
    }

    /**
     * CreateAsset initializes a new asset in the ledger
     * @param ctx Contract context
     * @param assetID Asset Id
     * @param color Color of asset
     * @param size Size of asset
     * @param owner Owner of asset
     * @param appraisedValue Appraised value of asset
     * @return Asset object
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset createAsset(final Context ctx, final String assetID, final String color,
                             final int size, final String owner, final int appraisedValue) {
        if (assetExists(ctx, assetID)) {
            String errorMessage = String.format("asset already exists: %s", assetID);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_ALREADY_EXISTS.toString());
        }

        Asset asset = new Asset(assetID, "asset", color, size, owner, appraisedValue);
        ctx.getStub().putState(assetID, asset.serialize());

        //  Create an index to enable color-based range queries, e.g. return all blue assets.
        //  An 'index' is a normal key-value entry in the ledger.
        //  The key is a composite key, with the elements that you want to range query on listed first.
        //  In our case, the composite key is based on indexName~color~name.
        //  This will enable very efficient state range queries based on composite keys matching indexName~color~*
        String colorNameIndexKey = ctx.getStub().createCompositeKey(index, asset.getColor(), assetID).toString();

        //  Save index entry to world state. Only the key name is needed, no need to store a duplicate copy of the asset.
        //  Note - passing a 'null' value will effectively delete the key from state, therefore we pass null character as value
        byte[] emptyBytes = new byte[] {0x00};
        ctx.getStub().putState(colorNameIndexKey, emptyBytes);

        return asset;
    }

    /**
     * Retrieve asset by asset id
     * @param ctx Contract context
     * @param assetID Asset id
     * @return Asset object
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Asset readAsset(final Context ctx, final String assetID) {
        byte[] assetBytes = ctx.getStub().getState(assetID);
        if (assetBytes == null || assetBytes.length == 0) {
            String errorMessage = String.format("asset %s does not exist", assetID);
            System.err.println(errorMessage);
            throw new ChaincodeException(errorMessage, AssetTransferErrors.ASSET_NOT_FOUND.toString());
        }

        return Asset.deserialize(assetBytes);
    }

    /**
     * DeleteAsset removes an asset key-value pair from the ledger
     * @param ctx Contract context
     * @param assetID asset id
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void deleteAsset(final Context ctx, final String assetID) {
        Asset asset = readAsset(ctx, assetID);
        ctx.getStub().delState(assetID);

        String colorNameIndexKey = ctx.getStub()
                .createCompositeKey(index, asset.getColor(), asset.getAssetID())
                .toString();
        ctx.getStub().delState(colorNameIndexKey);
    }

    /**
     * TransferAsset transfers an asset by setting a new owner name on the asset
     * @param ctx Contract context
     * @param assetID Asset id
     * @param newOwner New owner of asset
     * @return Asset object with new owner name
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Asset transferAsset(final Context ctx, final String assetID, final String newOwner) {
        Asset asset = readAsset(ctx, assetID);
        asset.setOwner(newOwner);
        ctx.getStub().putState(assetID, asset.serialize());
        return asset;
    }


    /**
     * AssetExists returns true when asset with given ID exists in the ledger.
     * @param ctx Contract context
     * @param assetID Asset id
     * @return Asset exists or not
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Boolean assetExists(final Context ctx, final String assetID) {
        byte[] assetBytes = ctx.getStub().getState(assetID);
        return assetBytes != null && assetBytes.length > 0;
    }


    /**
     * constructQueryResponseFromIterator constructs a slice of assets from the resultsIterator
     * @param resultsIterator Iterator of chaincode query
     * @return array of asset
     */
    private List<Asset> constructQueryResponseFromIterator(final QueryResultsIterator<KeyValue> resultsIterator) {
        List<Asset> assets = new ArrayList<>();
        for (KeyValue result: resultsIterator) {
            Asset asset = Asset.deserialize(result.getStringValue());
            assets.add(asset);
        }
        return assets;
    }

    /**
     * constructQueryResponseFromIterator constructs a slice of assets from the resultsIterator
     * @param resultsIterator Iterator of chaincode query
     * @return array of asset
     */
    private List<Asset> constructQueryResponseFromIterator(final QueryResultsIteratorWithMetadata<KeyValue> resultsIterator) {
        List<Asset> assets = new ArrayList<>();
        for (KeyValue result: resultsIterator) {
            Asset asset = Asset.deserialize(result.getStringValue());
            assets.add(asset);
        }
        return assets;
    }

    /**
     * GetAssetsByRange performs a range query based on the start and end keys provided.
     * Read-only function results are not typically submitted to ordering. If the read-only
     * results are submitted to ordering, or if the query is used in an update transaction
     * and submitted to ordering, then the committing peers will re-execute to guarantee that
     * result sets are stable between endorsement time and commit time. The transaction is
     * invalidated by the committing peers if the result set has changed between endorsement
     * time and commit time.
     * Therefore, range queries are a safe option for performing update transactions based on query results.
     * @param ctx Contract context
     * @param startKey start key
     * @param endKey end key
     * @return array of asset
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAssetsByRange(final Context ctx, final String startKey, final String endKey) {
        QueryResultsIterator<KeyValue> resultsIterator = ctx.getStub().getStateByRange(startKey, endKey);
        List<Asset> assets = constructQueryResponseFromIterator(resultsIterator);
        return genson.serialize(assets);
    }

    /**
     * TransferAssetByColor will transfer assets of a given color to a certain new owner.
     * Uses GetStateByPartialCompositeKey (range query) against color~name 'index'.
     * Committing peers will re-execute range queries to guarantee that result sets are stable
     * between endorsement time and commit time. The transaction is invalidated by the
     * committing peers if the result set has changed between endorsement time and commit time.
     * Therefore, range queries are a safe option for performing update transactions based on query results.
     * Example: GetStateByPartialCompositeKey/RangeQuery
     * @param ctx Contract context
     * @param color color
     * @param newOwner new owner name
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void transferAssetByColor(final Context ctx, final String color, final String newOwner) {
        QueryResultsIterator<KeyValue> coloredAssetResultsIterator =
                ctx.getStub().getStateByPartialCompositeKey(index, color);
        for (KeyValue result: coloredAssetResultsIterator) {
            String assetID = ctx.getStub().splitCompositeKey(result.getKey()).toString();
            Asset asset = readAsset(ctx, assetID);
            asset.setOwner(newOwner);
            ctx.getStub().putState(assetID, asset.serialize());
        }
    }

    /**
     * getQueryResultForQueryString executes the passed in query string.
     * The result set is built and returned as a byte array containing the JSON results.
     * @param ctx Contract context
     * @param queryString Query string
     * @return list of asset
     */
    private List<Asset> getQueryResultForQueryString(final Context ctx, final String queryString) {
        QueryResultsIterator<KeyValue> resultsIterator = ctx.getStub().getQueryResult(queryString);
        return constructQueryResponseFromIterator(resultsIterator);
    }

    /**
     * QueryAssetsByOwner queries for assets based on the owners name.
     * This is an example of a parameterized query where the query logic is baked into the chaincode,
     * and accepting a single query parameter (owner).
     * Only available on state databases that support rich query (e.g. CouchDB)
     * Example: Parameterized rich query
     * @param ctx Contract context
     * @param owner query owner
     * @return list of asset
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String queryAssetsByOwner(final Context ctx, final String owner) {
        String queryString = String.format("{\"selector\":{\"docType\":\"asset\",\"owner\":\"%s\"}}", owner);
        List<Asset> assets = getQueryResultForQueryString(ctx, queryString);
        return genson.serialize(assets);
    }

    /**
     * QueryAssets uses a query string to perform a query for assets.
     * Query string matching state database syntax is passed in and executed as is.
     * Supports ad hoc queries that can be defined at runtime by the client.
     * If this is not desired, follow the QueryAssetsForOwner example for parameterized queries.
     * Only available on state databases that support rich query (e.g. CouchDB)
     * Example: Ad hoc rich query
     * @param ctx Contract context
     * @param queryString query string
     * @return list of asset
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String queryAssets(final Context ctx, final String queryString) {
        List<Asset> assets = getQueryResultForQueryString(ctx, queryString);
        return genson.serialize(assets);
    }

    /**
     * GetAssetsByRangeWithPagination performs a range query based on the start and end key,
     * page size and a bookmark.
     * The number of fetched records will be equal to or lesser than the page size.
     * Paginated range queries are only valid for read only transactions.
     * Example: Pagination with Range Query
     * @param ctx Contract context
     * @param startKey start key
     * @param endKey end key
     * @param pageSize page size
     * @param bookmark booking mark
     * @return list of asset
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAssetsByRangeWithPagination(final Context ctx, final String startKey, final String endKey,
                                                      final int pageSize, final String bookmark) {
        QueryResultsIteratorWithMetadata<KeyValue> resultsIterator = ctx.getStub()
                .getStateByRangeWithPagination(startKey, endKey, pageSize, bookmark);
        List<Asset> assets = constructQueryResponseFromIterator(resultsIterator);
        return genson.serialize(assets);
    }

    /**
     * getQueryResultForQueryStringWithPagination executes the passed in query string with
     * pagination info. The result set is built and returned as a byte array containing the JSON results.
     * Bookmarks are uniquely generated by CouchDB for each query and represent a placeholder in the result set.
     * Pass the returned bookmark on the subsequent iteration of the query to retrieve the next set of results.
     * @param ctx Contract context
     * @param queryString query string
     * @param pageSize page size/ limit
     * @param bookmark book mark
     * @return result containing list of asset and total count
     */
    private PaginatedQueryResult getQueryResultForQueryStringWithPagination(final Context ctx, final String queryString,
                                                                            final int pageSize, final String bookmark) {
        QueryResultsIteratorWithMetadata<KeyValue> resultsIterator = ctx.getStub()
                .getQueryResultWithPagination(queryString, pageSize, bookmark);
        List<Asset> assets = constructQueryResponseFromIterator(resultsIterator);
        return new PaginatedQueryResult(assets,
                resultsIterator.getMetadata().getFetchedRecordsCount(),
                bookmark);
    }

    /**
     * QueryAssetsWithPagination uses a query string, page size and a bookmark to perform a query
     * for assets. Query string matching state database syntax is passed in and executed as is.
     * The number of fetched records would be equal to or lesser than the specified page size.
     * Supports ad hoc queries that can be defined at runtime by the client.
     * If this is not desired, follow the QueryAssetsForOwner example for parameterized queries.
     * Only available on state databases that support rich query (e.g. CouchDB)
     * Paginated queries are only valid for read only transactions.
     * Example: Pagination with Ad hoc Rich Query
     * @param ctx Contract context
     * @param queryString query string
     * @param pageSize page size/ limit
     * @param bookmark book mark
     * @return result containing list of asset and total count
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String queryAssetsWithPagination(final Context ctx, final String queryString,
                                                          final int pageSize, final String bookmark) {
        PaginatedQueryResult result = getQueryResultForQueryStringWithPagination(ctx, queryString,
                pageSize, bookmark);
        return genson.serialize(result);
    }

    /**
     * GetAssetHistory returns the chain of custody for an asset since issuance.
     * @param ctx Contract context
     * @param assetID asset id
     * @return list of asset history result containing asset object, tx id and timestamp
     */
    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String getAssetHistory(final Context ctx, final String assetID) {
        QueryResultsIterator<KeyModification> resultsIterator = ctx.getStub().getHistoryForKey(assetID);

        List<HistoryQueryResult> results = new ArrayList<HistoryQueryResult>();
        for (KeyModification keyModification: resultsIterator) {
            String assetJSON = keyModification.getStringValue();
            Asset asset;
            if (assetJSON == null || assetJSON.isEmpty()) {
                asset = new Asset(assetID);
            } else {
                asset = Asset.deserialize(assetJSON);
            }

            HistoryQueryResult result = new HistoryQueryResult(asset, keyModification.getTxId(),
                    keyModification.getTimestamp().getEpochSecond(), keyModification.isDeleted());
            results.add(result);
        }

        return genson.serialize(results);
    }

    /**
     * Init ledger, create seeds data
     * @param ctx Contract context
     */
    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void initLedger(final Context ctx) {
        Asset[] assets = {
                new Asset("asset1", "asset", "blue", 5, "Tomoko", 300),
                new Asset("asset2", "asset", "red", 5, "Brad", 400),
                new Asset("asset3", "asset", "green", 10, "Jin Soo", 500),
                new Asset("asset4", "asset", "yellow", 10, "Max", 600),
                new Asset("asset5", "asset", "black", 15, "Adriana", 700),
                new Asset("asset6", "asset", "white", 15, "Michel", 800),
        };

        for (Asset asset: assets) {
            createAsset(ctx, asset.getAssetID(), asset.getColor(),
                    asset.getSize(), asset.getOwner(), asset.getAppraisedValue());
        }
    }


}

