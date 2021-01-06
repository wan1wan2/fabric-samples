package org.example.fabric.samples.securedagreement;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class AssetForSale {

    private final String assetID;

    private final String objectType;

    private final String color;

    private final int size;

    private final String salt;

    public AssetForSale(String assetID, String objectType, String color, int size, String salt) {
        this.assetID = assetID;
        this.objectType = objectType;
        this.color = color;
        this.size = size;
        this.salt = salt;
    }

    public byte[] serialize() {
        String jsonString = new JSONObject(this).toString();
        return jsonString.getBytes(StandardCharsets.UTF_8);
    }
}
