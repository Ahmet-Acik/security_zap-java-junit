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

        public class ZapAutomation {
            private static final Logger logger = LoggerFactory.getLogger(ZapAutomation.class);

            private static final String ZAP_ADDRESS = "localhost";
            private static final int ZAP_PORT = 8080;
            private static final String TARGET_URL = "https://juice-shop.herokuapp.com";

            public static String getZapAddress() {
                return ZAP_ADDRESS;
            }

            public static int getZapPort() {
                return ZAP_PORT;
            }

            public static void main(String[] args) {
                String zapApiKey = loadApiKey();

                if (zapApiKey == null || zapApiKey.isEmpty()) {
                    logger.error("ZAP API key is not set. Please set it in the configuration file or as an environment variable.");
                    return;
                }

                logger.info("Initializing ZAP API client...");
                ClientApi api = new ClientApi(ZAP_ADDRESS, ZAP_PORT, zapApiKey);

                try {
                    logger.info("Starting a new ZAP session...");
                    api.core.newSession("automation-session", "true");

                    logger.info("Accessing target: {}", TARGET_URL);
                    api.spider.scan(TARGET_URL, null, null, null, null);

                    while (Integer.parseInt(api.spider.status("").toString()) < 100) {
                        logger.info("Spider progress: {}%", api.spider.status(""));
                        Thread.sleep(1000);
                    }
                    logger.info("Spider completed.");

                    logger.info("Starting active scan...");
                    api.ascan.scan(TARGET_URL, "true", "false", null, null, null);

                    while (Integer.parseInt(api.ascan.status("").toString()) < 100) {
                        logger.info("Active scan progress: {}%", api.ascan.status(""));
                        Thread.sleep(1000);
                    }
                    logger.info("Active scan completed.");

                    logger.info("Generating scan report...");
                    String report = new String(api.core.htmlreport());
                    Files.write(Paths.get("zap-report.html"), report.getBytes());
                    logger.info("Report saved as zap-report.html");

                } catch (ClientApiException | InterruptedException | IOException e) {
                    logger.error("An error occurred during the ZAP automation process.", e);
                }
            }

     private static String loadApiKey() {
         Properties properties = new Properties();
         try (FileInputStream fis = new FileInputStream(
                 ZapAutomation.class.getClassLoader().getResource("config.properties").getFile())) {
             properties.load(fis);
             String zapApiKey = properties.getProperty("ZAP_API_KEY");
             if (zapApiKey != null && !zapApiKey.isEmpty()) {
                 logger.info("Using ZAP API key from configuration file.");
                 return zapApiKey;
             }
         } catch (IOException | NullPointerException e) {
             logger.warn("Configuration file not found or failed to load. Falling back to environment variable.");
         }

         String zapApiKey = System.getenv("ZAP_API_KEY");
         if (zapApiKey != null && !zapApiKey.isEmpty()) {
             logger.info("Using ZAP API key from environment variable.");
             return zapApiKey;
         }

         return null;
     }
        }