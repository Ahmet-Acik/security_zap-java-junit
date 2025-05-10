package org.ahmet;

import java.io.IOException;

public class ZapAutomationTestHelper {

    private final ZapAutomationUtil zapAutomationUtil;

    public ZapAutomationTestHelper(String zapAddress, int zapPort, String zapApiKey, String targetUrl) {
        this.zapAutomationUtil = new ZapAutomationUtil(zapAddress, zapPort, zapApiKey, targetUrl);
    }

    public void runFullScan(String sessionName, String reportPath) throws IOException {
        zapAutomationUtil.startSession(sessionName);
        zapAutomationUtil.runSpider();
        zapAutomationUtil.runActiveScan();
        zapAutomationUtil.generateReport(reportPath);
    }
}