package com.example.books.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "google.books")
public class GoogleBooksProperties {

    private String baseUrl;
    private String apiKey;
    private final Api api = new Api();
    private int connectTimeoutMs = 5000;
    private int readTimeoutMs = 10000;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getApiKey() {
        return api.getKey() != null ? api.getKey() : apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
        this.api.setKey(apiKey);
    }

    public Api getApi() {
        return api;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public static class Api {
        private String key;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }
    }
}
