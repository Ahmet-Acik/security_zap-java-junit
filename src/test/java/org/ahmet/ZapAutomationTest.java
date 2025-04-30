package org.ahmet;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ZapAutomationTest {
    private static final Logger logger = LoggerFactory.getLogger(ZapAutomationTest.class);

    private static final String TARGET_URL = "https://juice-shop.herokuapp.com";

    @Test
    public void testZapUrlAutomation() {
        assertEquals("localhost", ZapAutomationUtil.getZapAddress());
        assertEquals(8080, ZapAutomationUtil.getZapPort());
    }

    @Test
    public void testZapAutomation() throws InterruptedException, IOException, ClientApiException {
        String zapApiKey = ZapAutomationUtil.loadApiKey();
        assertNotNull(zapApiKey, "ZAP API key should not be null");

        ClientApi api = new ClientApi(ZapAutomationUtil.getZapAddress(), ZapAutomationUtil.getZapPort(), zapApiKey);

        api.core.newSession("automation-session", "true");

        api.spider.scan(TARGET_URL, null, null, null, null);
        while (Integer.parseInt(api.spider.status("").toString()) < 100) {
            Thread.sleep(1000);
        }

        api.ascan.scan(TARGET_URL, "true", "false", null, null, null);
        while (Integer.parseInt(api.ascan.status("").toString()) < 100) {
            Thread.sleep(1000);
        }

        String report = new String(api.core.htmlreport());
        Files.write(Paths.get("zap-report.html"), report.getBytes());
    }
}