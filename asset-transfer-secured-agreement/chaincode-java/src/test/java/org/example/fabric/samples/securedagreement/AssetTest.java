package org.example.fabric.samples.securedagreement;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import org.hyperledger.fabric.shim.ChaincodeException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class AssetTest {

    @Nested
    class ConvertTest {

        @Test
        public void isSerializable() {
            Asset asset = new Asset("asset1", "Org1MSP", "public text");
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
            Asset asset = new Asset("asset1", "Org1MSP", "public text");
            String assetString = asset.toString();
            assertThat(assetString).contains("asset1");
            assertThat(assetString).contains("Org1MSP");
            assertThat(assetString).contains("public text");
        }
    }

    @Nested
    class EqualityTest {

        @Test
        public void isEquality() {
            Asset asset = new Asset("asset1", "Org1MSP", "public text");
            assertThat(asset).isEqualTo(asset);

            Asset asset2 = new Asset("asset1", "Org1MSP", "public text");
            assertThat(asset).isEqualTo(asset2);

            asset2.setOwnerOrg("Org2MSP");
            asset2.setPublicDescription("public text 2");
            assertThat(asset).isNotEqualTo(asset2);
        }

        @Test
        public void isNotEquality() {
            Asset asset = new Asset("asset1", "Org1MSP", "public text");
            Asset asset2 = new Asset("asset2", "Org1MSP", "public text");

            assertThat(asset).isNotEqualTo(asset2);
        }
    }
}
