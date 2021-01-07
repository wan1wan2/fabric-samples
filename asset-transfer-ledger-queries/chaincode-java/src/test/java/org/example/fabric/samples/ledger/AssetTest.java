package org.example.fabric.samples.ledger;

import org.example.fabric.samples.ledger.models.Asset;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

public class AssetTest {

    @Nested
    class ConvertTest {

        @Test
        public void isSerializable() {
            Asset asset = new Asset("asset1", "asset", "red", 16, "Org1MSP", 200);
            byte[] serializeData = asset.serialize();
            assertThat(serializeData).isNotNull();

            Asset asset2 = Asset.deserialize(serializeData);
            assertThat(asset2).isNotNull();
            assertThat(asset2.getAssetID()).isEqualTo(asset.getAssetID());
        }

        @Test
        public void isNotDeserializable() {
            String text = "{}";
            Throwable thrown = catchThrowable(() -> Asset.deserialize(text));
            assertThat(thrown).isInstanceOf(ChaincodeException.class).hasNoCause();
            assertThat(((ChaincodeException) thrown).getPayload()).isEqualTo("DATA_ERROR".getBytes());
        }

        @Test
        public void assetToStringSuccess() {
            Asset asset = new Asset("asset1", "asset", "red", 16, "Org1MSP", 200);
            String assetString = asset.toString();
            assertThat(assetString).contains("asset1");
            assertThat(assetString).contains("Org1MSP");
            assertThat(assetString).contains("asset");
        }
    }

    @Nested
    class EqualityTest {

        @Test
        public void isEquality() {
            Asset asset = new Asset("asset1", "asset", "red", 16, "Org1MSP", 200);
            assertThat(asset).isEqualTo(asset);

            Asset asset2 = new Asset("asset1", "asset", "red", 16, "Org1MSP", 200);
            assertThat(asset).isEqualTo(asset2);
        }

        @Test
        public void isNotEquality() {
            Asset asset = new Asset("asset1", "asset", "red", 16, "Org1MSP", 200);
            Asset asset2 = new Asset("asset2", "asset", "red", 16, "Org1MSP", 200);
            assertThat(asset).isNotEqualTo(asset2);
        }
    }

}
