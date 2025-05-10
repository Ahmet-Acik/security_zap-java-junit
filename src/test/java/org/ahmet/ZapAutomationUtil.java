package org.ahmet;

            import org.slf4j.Logger;
            import org.slf4j.LoggerFactory;
            import org.zaproxy.clientapi.core.ClientApi;
            import org.zaproxy.clientapi.core.ClientApiException;

            import java.io.FileInputStream;
            import java.io.IOException;
            import java.nio.file.Files;
            import java.nio.file.Paths;
            import java.util.Properties;

            public class ZapAutomationUtil {
                private static final Logger logger = LoggerFactory.getLogger(ZapAutomationUtil.class);

                private static final String ZAP_ADDRESS = "localhost";
                private static final int ZAP_PORT = 8080;

                private final ClientApi api;
                private final String targetUrl;

                public ZapAutomationUtil(String zapAddress, int zapPort, String zapApiKey, String targetUrl) {
                    this.api = new ClientApi(zapAddress, zapPort, zapApiKey);
                    this.targetUrl = targetUrl;
                    logger.info("ZapAutomationUtil initialized with target URL: {}", targetUrl);
                }

                public static String getZapAddress() {
                    return ZAP_ADDRESS;
                }

                public static int getZapPort() {
                    return ZAP_PORT;
                }

                public static String loadApiKey() {
                    String zapApiKey = System.getenv("ZAP_API_KEY");
                    if (zapApiKey != null && !zapApiKey.isEmpty()) {
                        logger.info("ZAP API key loaded from environment variable.");
                        return zapApiKey;
                    }

                    Properties properties = new Properties();
                    var resource = ZapAutomationUtil.class.getClassLoader().getResource("config.properties");
                    if (resource != null) {
                        try (FileInputStream fis = new FileInputStream(resource.getFile())) {
                            properties.load(fis);
                            zapApiKey = properties.getProperty("ZAP_API_KEY");
                            if (zapApiKey != null && !zapApiKey.isEmpty()) {
                                logger.info("ZAP API key loaded from configuration file.");
                                return zapApiKey;
                            }
                        } catch (IOException e) {
                            logger.warn("Failed to load API key from configuration file.", e);
                        }
                    } else {
                        logger.warn("Configuration file 'config.properties' not found.");
                    }

                    logger.error("ZAP API key could not be loaded from environment variable or configuration file.");
                    return null;
                }

                public void startSession(String sessionName) {
                    try {
                        logger.info("Starting a new ZAP session: {}", sessionName);
                        api.core.newSession(sessionName, "true");
                        logger.info("ZAP session '{}' started successfully.", sessionName);
                    } catch (ClientApiException e) {
                        logger.error("Failed to start ZAP session '{}'.", sessionName, e);
                    }
                }

                public void runSpider() {
                    try {
                        logger.info("Starting spider scan on target: {}", targetUrl);
                        api.spider.scan(targetUrl, null, null, null, null);

                        String status;
                        while ((status = api.spider.status("").toString()) != null && Integer.parseInt(status) < 100) {
                            logger.info("Spider progress: {}%", status);
                            Thread.sleep(1000);
                        }
                        logger.info("Spider scan completed successfully.");
                    } catch (ClientApiException e) {
                        logger.error("Spider scan failed for target: {}", targetUrl, e);
                    } catch (InterruptedException e) {
                        logger.error("Spider scan interrupted.", e);
                        Thread.currentThread().interrupt();
                    }
                }

                public void runActiveScan() {
                    try {
                        logger.info("Starting active scan on target: {}", targetUrl);
                        api.ascan.scan(targetUrl, "true", "false", null, null, null);

                        String status;
                        while ((status = api.ascan.status("").toString()) != null && Integer.parseInt(status) < 100) {
                            logger.info("Active scan progress: {}%", status);
                            Thread.sleep(1000);
                        }
                        logger.info("Active scan completed successfully.");
                    } catch (ClientApiException e) {
                        logger.error("Active scan failed for target: {}", targetUrl, e);
                    } catch (InterruptedException e) {
                        logger.error("Active scan interrupted.", e);
                        Thread.currentThread().interrupt();
                    }
                }

                public void generateReport(String reportPath) {
                    try {
                        logger.info("Generating scan report...");
                        String report = new String(api.core.htmlreport());
                        Files.write(Paths.get(reportPath), report.getBytes());
                        logger.info("Report saved at: {}", reportPath);
                    } catch (ClientApiException e) {
                        logger.error("Failed to generate scan report.", e);
                    } catch (IOException e) {
                        logger.error("Failed to save scan report to path: {}", reportPath, e);
                    }
                }
            }