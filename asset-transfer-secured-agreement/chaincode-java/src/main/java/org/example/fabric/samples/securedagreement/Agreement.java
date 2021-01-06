package org.example.fabric.samples.securedagreement;

import org.hyperledger.fabric.shim.ChaincodeException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class Agreement {

    private final String assetID;

    private final int price;

    private final String tradeID;

    public int getPrice() {
        return price;
    }

    public Agreement(String assetID, int price, String tradeID) {
        this.assetID = assetID;
        this.price = price;
        this.tradeID = tradeID;
    }

    public static Agreement deserialize(byte[] agreementJSON) {
        return deserialize(new String(agreementJSON, StandardCharsets.UTF_8));
    }

    public static Agreement deserialize(String agreementJSON) {
        try {
            JSONObject jsonObject = new JSONObject(agreementJSON);
            final String assetId = jsonObject.getString("asset_id");
            final int price = jsonObject.getInt("price");
            final String tradeID = jsonObject.getString("trade_id");
            return new Agreement(assetId, price, tradeID);
        } catch (Exception e) {
            throw new ChaincodeException("Deserialize error: " + e.getMessage(), "DATA_ERROR");
        }
    }

    public byte[] serialize() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("asset_id", this.assetID);
        jsonObject.put("price", this.price);
        jsonObject.put("trade_id", this.tradeID);
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }
}
