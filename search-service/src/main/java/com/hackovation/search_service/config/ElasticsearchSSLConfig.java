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

@Configuration
public class ElasticsearchSSLConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticUris;

    @Value("${spring.elasticsearch.username}")
    private String user;

    @Value("${spring.elasticsearch.password}")
    private String pass;

    @Value("${ssl.bundle.jks.es-ssl.truststore.location}")
    private Resource trustResource;

    @Value("${ssl.bundle.jks.es-ssl.truststore.password}")
    private String trustPwd;

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(trimProtocol(elasticUris))
                .usingSsl(buildSslContext())
                .withBasicAuth(user, pass)
                .build();
    }

    private SSLContext buildSslContext() {
        try (InputStream is = trustResource.getInputStream()) {
            KeyStore ts = KeyStore.getInstance(KeyStore.getDefaultType());
            ts.load(is, trustPwd.toCharArray());

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

    /** Remove protocol prefix so spring/ClientConfiguration.connectedTo(...) gets host:port only. */
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
