package org.example.fabric.samples.ledger.models;

public final class HistoryQueryResult {

    private Asset record;

    private String txId;

    private long timestamp;

    private Boolean isDelete;

    public Asset getRecord() {
        return record;
    }

    public void setRecord(final Asset value) {
        this.record = value;
    }

    public String getTxId() {
        return txId;
    }

    public void setTxId(final String value) {
        this.txId = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(final long value) {
        this.timestamp = value;
    }

    public Boolean getDelete() {
        return isDelete;
    }

    public void setDelete(final Boolean value) {
        isDelete = value;
    }

    public HistoryQueryResult(final Asset record, final String txId, final long timestamp, final Boolean isDelete) {
        this.record = record;
        this.txId = txId;
        this.timestamp = timestamp;
        this.isDelete = isDelete;
    }

    public HistoryQueryResult() {

    }
}
