package org.example.fabric.samples.ledger.models;

import java.util.List;

public final class PaginatedQueryResult {

    private List<Asset> records;

    private int fetchedRecordsCount;

    private String bookmark;

    public List<Asset> getRecords() {
        return records;
    }

    public void setRecords(final List<Asset> value) {
        this.records = value;
    }

    public int getFetchedRecordsCount() {
        return fetchedRecordsCount;
    }

    public void setFetchedRecordsCount(final int value) {
        this.fetchedRecordsCount = value;
    }

    public String getBookmark() {
        return bookmark;
    }

    public void setBookmark(final String value) {
        this.bookmark = value;
    }

    public PaginatedQueryResult(final List<Asset> records, final int fetchedRecordsCount, final String bookmark) {
        this.records = records;
        this.fetchedRecordsCount = fetchedRecordsCount;
        this.bookmark = bookmark;
    }

    public PaginatedQueryResult() {
    }

}
