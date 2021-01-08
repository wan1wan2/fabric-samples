package org.example.fabric.samples.ledger.models;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@DataType()
public final class Asset {

    @Property()
    private String assetID;

    @Property()
    private String docType;

    @Property()
    private String color;

    @Property()
    private int size;

    @Property()
    private String owner;

    @Property()
    private int appraisedValue;

    public String getAssetID() {
        return assetID;
    }

    public void setAssetID(final String value) {
        this.assetID = value;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(final String value) {
        this.docType = value;
    }

    public String getColor() {
        return color;
    }

    public void setColor(final String value) {
        this.color = value;
    }

    public int getSize() {
        return size;
    }

    public void setSize(final int value) {
        this.size = value;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(final String value) {
        this.owner = value;
    }

    public int getAppraisedValue() {
        return appraisedValue;
    }

    public void setAppraisedValue(final int value) {
        this.appraisedValue = value;
    }

    public Asset(final String assetID, final  String docType, final  String color,
                 final int size, final String owner, final int appraisedValue) {
        this.assetID = assetID;
        this.docType = docType;
        this.color = color;
        this.size = size;
        this.owner = owner;
        this.appraisedValue = appraisedValue;
    }

    public Asset(final String assetID) {
        this.assetID = assetID;
    }

    public Asset() {

    }

    @Override
    public String toString() {
        return "Asset{"
                + "assetID='" + assetID + '\''
                + ", docType='" + docType + '\''
                + ", color='" + color + '\''
                + ", size=" + size
                + ", owner='" + owner + '\''
                + ", appraisedValue=" + appraisedValue
                + '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Asset asset = (Asset) o;
        return Objects.equals(assetID, asset.getAssetID())
                && Objects.equals(docType, asset.getDocType())
                && Objects.equals(owner, asset.getOwner())
                && Objects.equals(size, asset.getSize())
                && Objects.equals(color, asset.getColor())
                && Objects.equals(appraisedValue, asset.getAppraisedValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetID, docType, color, size, owner, appraisedValue);
    }

    public byte[] serialize() {
        return toJSON().getBytes(StandardCharsets.UTF_8);
    }

    public static Asset deserialize(final byte[] assetJSON) {
        return deserialize(new String(assetJSON, StandardCharsets.UTF_8));
    }

    public static Asset deserialize(final String assetJSON) {
        try {
            JSONObject jsonObject = new JSONObject(assetJSON);
            final String assetId = jsonObject.getString("assetID");
            final String docType = jsonObject.getString("docType");
            final String owner = jsonObject.getString("owner");
            final String color = jsonObject.getString("color");
            final int size = jsonObject.getInt("size");
            final int appraisedValue = jsonObject.getInt("appraisedValue");
            return new Asset(assetId, docType, color, size, owner, appraisedValue);
        } catch (Exception e) {
            throw new ChaincodeException("Deserialize error: " + e.getMessage(), "DATA_ERROR");
        }
    }

    public String toJSON() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("assetID", assetID);
        jsonObject.put("docType", docType);
        jsonObject.put("owner", owner);
        jsonObject.put("size", size);
        jsonObject.put("color", color);
        jsonObject.put("appraisedValue", appraisedValue);
        return jsonObject.toString();
    }
}
