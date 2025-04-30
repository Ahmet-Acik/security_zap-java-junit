package org.ahmet;

    import org.zaproxy.clientapi.core.ClientApi;
    import org.zaproxy.clientapi.core.ClientApiException;

    import java.io.FileInputStream;
    import java.io.IOException;
    import java.nio.file.Files;
    import java.nio.file.Paths;
    import java.util.Properties;

    public class ZapAutomationUtil {
        private static final String ZAP_ADDRESS = "localhost";
        private static final int ZAP_PORT = 8080;

        private final ClientApi api;
        private final String targetUrl;

        public ZapAutomationUtil(String zapAddress, int zapPort, String zapApiKey, String targetUrl) {
            this.api = new ClientApi(zapAddress, zapPort, zapApiKey);
            this.targetUrl = targetUrl;
        }

        public static String getZapAddress() {
            return ZAP_ADDRESS;
        }

        public static int getZapPort() {
            return ZAP_PORT;
        }

        public static String loadApiKey() {
            Properties properties = new Properties();
            try (FileInputStream fis = new FileInputStream(
                    ZapAutomationUtil.class.getClassLoader().getResource("config.properties").getFile())) {
                properties.load(fis);
                String zapApiKey = properties.getProperty("ZAP_API_KEY");
                if (zapApiKey != null && !zapApiKey.isEmpty()) {
                    return zapApiKey;
                }
            } catch (IOException | NullPointerException e) {
                // Log or handle the exception as needed
            }

            String zapApiKey = System.getenv("ZAP_API_KEY");
            if (zapApiKey != null && !zapApiKey.isEmpty()) {
                return zapApiKey;
            }

            return null;
        }

        public void startSession(String sessionName) throws ClientApiException {
            api.core.newSession(sessionName, "true");
        }

        public void runSpider() throws ClientApiException, InterruptedException {
            api.spider.scan(targetUrl, null, null, null, null);

            while (Integer.parseInt(api.spider.status("").toString()) < 100) {
                Thread.sleep(1000);
            }
        }

        public void runActiveScan() throws ClientApiException, InterruptedException {
            api.ascan.scan(targetUrl, "true", "false", null, null, null);

            while (Integer.parseInt(api.ascan.status("").toString()) < 100) {
                Thread.sleep(1000);
            }
        }

        public void generateReport(String reportPath) throws ClientApiException, IOException {
            String report = new String(api.core.htmlreport());
            Files.write(Paths.get(reportPath), report.getBytes());
        }
    }