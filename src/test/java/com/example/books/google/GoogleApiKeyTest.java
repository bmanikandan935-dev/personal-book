package com.example.books.google;

import com.example.books.service.GoogleBookService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GoogleApiKeyTest {

    static MockWebServer server;

    @BeforeAll
    static void startServer() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void stopServer() throws IOException {
        server.shutdown();
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("google.books.base-url", () -> server.url("/").toString());
        registry.add("google.books.api.key", () -> "required-test-key");
    }

    @Autowired
    private GoogleBookService googleBookService;

    @Test
    void googleCalls_includeConfiguredApiKey() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"kind\":\"books#volumes\",\"totalItems\":0,\"items\":[]}"));

        googleBookService.searchBooks("effective java", 10, 0);

        RecordedRequest request = server.takeRequest();
        assertThat(request.getRequestUrl()).isNotNull();
        assertThat(request.getRequestUrl().queryParameter("key")).isEqualTo("required-test-key");
    }
}
