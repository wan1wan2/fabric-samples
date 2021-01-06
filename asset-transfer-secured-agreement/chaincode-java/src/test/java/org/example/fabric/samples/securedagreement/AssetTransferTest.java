package org.example.fabric.samples.securedagreement;

import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class AssetTransferTest {
    @Nested
    class InvokeWriteTransaction {

        @Test
        public void createAssetShouldBeSuccess() {
            AssetTransfer contract = new AssetTransfer();
            Context ctx = mock(Context.class);
            ChaincodeStub stub = mock(ChaincodeStub.class);
            when(ctx.getStub()).thenReturn(stub);
            when(stub.getMspId()).thenReturn(testOrgOneMSP);
            ClientIdentity ci = mock(ClientIdentity.class);
            when(ci.getId()).thenReturn(testOrg1Client);
            when(ci.getMSPID()).thenReturn(testOrgOneMSP);
            when(ctx.getClientIdentity()).thenReturn(ci);

            Map<String, byte[]> transientMap = new HashMap<>();
            AssetForSale assetForSale = new AssetForSale("asset1", "asset_properties"
                    , "blue", 16, "a94a8fe5ccb19ba61c4c0873d391e987982fbbd3");
            transientMap.put("asset_properties", assetForSale.serialize());
            when(stub.getTransient()).thenReturn(transientMap);

            Asset asset = contract.createAsset(ctx, "asset1", "public description text");
            assertThat(asset).isNotNull();
            assertThat(asset).isEqualTo(getTestAsset());
        }
    }

    private Asset getTestAsset() {
        return new Asset("asset1", testOrgOneMSP, "public description text");
    }

    private static String testOrg1Client = "testOrg1User";
    private static String testOrgOneMSP = "TestOrg1";
}
