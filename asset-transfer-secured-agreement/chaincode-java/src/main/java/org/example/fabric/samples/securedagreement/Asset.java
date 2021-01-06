package org.example.fabric.samples.securedagreement;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

@DataType()
public final class Asset {

    @Property()
    private final String assetID;

    @Property()
    private final String objectType;

    @Property()
    private String ownerOrg;

    @Property()
    private String publicDescription;

    public String getAssetID() {
        return assetID;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setOwnerOrg(final String value) {
        this.ownerOrg = value;
    }

    public String getOwnerOrg() {
        return ownerOrg;
    }

    public void setPublicDescription(final String value) {
        this.publicDescription = value;
    }

    public String getPublicDescription() {
        return publicDescription;
    }

    public Asset(final String assetID, final String ownerOrg, final String publicDescription) {
        this.assetID = assetID;
        this.ownerOrg = ownerOrg;
        this.publicDescription = publicDescription;
        this.objectType = "asset";
    }

    public byte[] serialize() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("assetID", assetID);
        jsonObject.put("ownerOrg", ownerOrg);
        jsonObject.put("publicDescription", publicDescription);
        jsonObject.put("objectType", objectType);
        return jsonObject.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static Asset deserialize(final byte[] assetJSON) {
        return deserialize(new String(assetJSON, StandardCharsets.UTF_8));
    }

    public static Asset deserialize(final String assetJSON) {
        try {
            JSONObject jsonObject = new JSONObject(assetJSON);
            final String assetId = jsonObject.getString("assetID");
            final String ownerOrg = jsonObject.getString("ownerOrg");
            final String publicDescription = jsonObject.getString("publicDescription");
            return new Asset(assetId, ownerOrg, publicDescription);
        } catch (Exception e) {
            throw new ChaincodeException("Deserialize error: " + e.getMessage(), "DATA_ERROR");
        }
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
        return Objects.equals(assetID, asset.getAssetID()) && Objects.equals(objectType, asset.getObjectType())
                && Objects.equals(ownerOrg, asset.getOwnerOrg())
                && Objects.equals(publicDescription, asset.getPublicDescription());
    }

    @Override
    public String toString() {
        return "Asset{"
                + "assetID='" + assetID + '\''
                + ", objectType='" + objectType + '\''
                + ", ownerOrg='" + ownerOrg + '\''
                + ", publicDescription='" + publicDescription + '\''
                + '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(assetID, objectType, ownerOrg, publicDescription);
    }
}
