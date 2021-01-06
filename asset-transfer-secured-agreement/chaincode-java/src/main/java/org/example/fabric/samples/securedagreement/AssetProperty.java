package org.example.fabric.samples.securedagreement;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class AssetProperty {

    private final String assetID;

    private final String objectType;

    private final String color;

    private final int size;

    private final String salt;

    public AssetProperty(String assetID, String objectType, String color, int size, String salt) {
        this.assetID = assetID;
        this.objectType = objectType;
        this.color = color;
        this.size = size;
        this.salt = salt;
    }

    public byte[] serialize() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("assetID", assetID);
        jsonObject.put("objectType", objectType);
        jsonObject.put("color", color);
        jsonObject.put("size", size);
        jsonObject.put("salt", salt);
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }
}