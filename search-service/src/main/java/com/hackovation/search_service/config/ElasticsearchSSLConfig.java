package com.hackovation.search_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

@Configuration
public class ElasticsearchSSLConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticUris;

    @Value("${spring.elasticsearch.username}")
    private String user;

    @Value("${spring.elasticsearch.password}")
    private String pass;

    /** Path to the ca.crt PEM file produced by the setup container. */
    @Value("${ssl.bundle.pem.es-ssl.truststore.certificate}")
    private Resource caCertResource;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(trimProtocol(elasticUris))
                .usingSsl(buildSslContext())
                .withBasicAuth(user, pass)
                .build();
    }

    private SSLContext buildSslContext() {
        try (InputStream is = caCertResource.getInputStream()) {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate caCert = (X509Certificate) cf.generateCertificate(is);

            KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
            ts.load(null, null);
            ts.setCertificateEntry("es-ca", caCert);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                    TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(ts);

            SSLContext ssl = SSLContext.getInstance("TLS");
            ssl.init(null, tmf.getTrustManagers(), null);
            return ssl;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to build SSLContext for Elasticsearch", e);
        }
    }

    /** Remove protocol prefix so ClientConfiguration.connectedTo(...) gets host:port only. */
    private String trimProtocol(String url) {
        if (url.startsWith("https://")) {
            return url.substring("https://".length());
        }
        if (url.startsWith("http://")) {
            return url.substring("http://".length());
        }
        return url;
    }
}
