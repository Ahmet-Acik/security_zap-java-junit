package org.ahmet;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ZapAutomationTest {

//    private static final String TARGET_URL = "https://juice-shop.herokuapp.com";
    private static final String TARGET_URL = "https://frendioriginal.com/";

    @Test
    public void testZapUrlAutomation() {
        assertEquals("localhost", ZapAutomationUtil.getZapAddress());
        assertEquals(8080, ZapAutomationUtil.getZapPort());
    }

    @Test
    public void testZapAutomation() throws IOException {
        String zapApiKey = ZapAutomationUtil.loadApiKey();
        assertNotNull(zapApiKey, "ZAP API key should not be null");

        ZapAutomationTestHelper helper = new ZapAutomationTestHelper(
                ZapAutomationUtil.getZapAddress(),
                ZapAutomationUtil.getZapPort(),
                zapApiKey,
                TARGET_URL
        );

        helper.runFullScan("automation-session", "zap-report.html");
    }
}