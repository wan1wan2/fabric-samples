package org.example.fabric.samples.securedagreement;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

@DataType()
public class AssetReceipt {

    @Property()
    private final String assetID;

    @Property()
    private final int price;

    @Property()
    private final long timestamp;

    public AssetReceipt(String assetID, int price, long timestamp) {
        this.assetID = assetID;
        this.price = price;
        this.timestamp = timestamp;
    }

    public byte[] serialize() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("assetID", assetID);
        jsonObject.put("price", price);
        jsonObject.put("timestamp", timestamp);
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }
}
