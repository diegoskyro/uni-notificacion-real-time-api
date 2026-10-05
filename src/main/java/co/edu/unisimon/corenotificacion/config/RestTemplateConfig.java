package co.edu.unisimon.corenotificacion.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate restTemplate() {
        try {
            java.util.logging.Logger.getLogger("org.hibernate").setLevel(java.util.logging.Level.OFF);
            
            javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[] {
                new javax.net.ssl.X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                    public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {}
                }
            };

            javax.net.ssl.SSLContext sc = javax.net.ssl.SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            
            java.net.http.HttpClient httpClient = java.net.http.HttpClient.newBuilder()
                .sslContext(sc)
                .build();

            org.springframework.http.client.JdkClientHttpRequestFactory factory = 
                new org.springframework.http.client.JdkClientHttpRequestFactory(httpClient);

            return new RestTemplate(factory);
        } catch (Exception e) {
            return new RestTemplate();
        }
    }
}
