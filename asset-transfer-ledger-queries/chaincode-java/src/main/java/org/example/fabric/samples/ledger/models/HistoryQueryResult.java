package org.example.fabric.samples.ledger.models;

public final class HistoryQueryResult {

    private final Asset record;

    private final String txId;

    private final long timestamp;

    private final Boolean isDelete;

    public HistoryQueryResult(final Asset record, final String txId, final long timestamp, final Boolean isDelete) {
        this.record = record;
        this.txId = txId;
        this.timestamp = timestamp;
        this.isDelete = isDelete;
    }
}
