package org.example.fabric.samples.ledger.models;

import java.util.List;

public final class PaginatedQueryResult {

    private final List<Asset> records;

    private final int fetchedRecordsCount;

    private final String bookmark;

    public List<Asset> getRecords() {
        return records;
    }

    public int getFetchedRecordsCount() {
        return fetchedRecordsCount;
    }

    public String getBookmark() {
        return bookmark;
    }

    public PaginatedQueryResult(final List<Asset> records, final int fetchedRecordsCount, final String bookmark) {
        this.records = records;
        this.fetchedRecordsCount = fetchedRecordsCount;
        this.bookmark = bookmark;
    }

}
