package com.example.books.google;

import com.example.books.dto.GoogleBookResponse;
import com.example.books.dto.GoogleBookSearchResponse;
import com.example.books.exception.BookNotFoundException;
import com.example.books.exception.GoogleApiException;
import com.example.books.exception.UpstreamServiceException;
import com.example.books.service.GoogleBookService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class GoogleBookServiceMockServerTest {

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
        registry.add("google.books.api.key", () -> "test-key");
        registry.add("google.books.connect-timeout-ms", () -> 100);
        registry.add("google.books.read-timeout-ms", () -> 100);
    }

    @Autowired
    private GoogleBookService googleBookService;

    @Test
    void search_mocked_returnsEffectiveJava() throws IOException {
        Path path = Paths.get("src", "test", "resources", "effectivejava.json");
        String body = Files.readString(path);
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        GoogleBookSearchResponse result = googleBookService.searchBooks("effective+java", 5, 0);
        assertThat(result).isNotNull();
        assertThat(result.kind()).isEqualTo("books#volumes");
        assertThat(result.items()).isNotEmpty();
        GoogleBookSearchResponse.Item first = result.items().get(0);
        assertThat(first.volumeInfo().title()).isEqualTo("Effective Java");
    }

    @Test
    void fetchVolumeById_includesApiKey() throws Exception {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody("{\"id\":\"volume-1\",\"volumeInfo\":{\"title\":\"Clean Code\",\"authors\":[\"Robert C. Martin\"],\"pageCount\":464}}"));

        googleBookService.fetchVolumeById("volume-1");

        RecordedRequest request = server.takeRequest();
        assertThat(request.getPath()).isEqualTo("/volumes/volume-1?key=test-key");
    }

    @Test
    void fetchVolumeById_404_throwsNotFound() {
        server.enqueue(new MockResponse().setResponseCode(404));

        assertThatThrownBy(() -> googleBookService.fetchVolumeById("missing"))
                .isInstanceOf(BookNotFoundException.class);
    }

    @Test
    void fetchVolumeById_500_throwsGoogleApiException() {
        server.enqueue(new MockResponse().setResponseCode(500));

        assertThatThrownBy(() -> googleBookService.fetchVolumeById("boom"))
                .isInstanceOf(GoogleApiException.class)
                .hasMessageContaining("server error");
    }

    @Test
    void fetchVolumeById_504_throwsUpstreamException() {
        server.enqueue(new MockResponse().setResponseCode(504));

        assertThatThrownBy(() -> googleBookService.fetchVolumeById("timeout"))
                .isInstanceOf(UpstreamServiceException.class)
                .hasMessageContaining("upstream")
                .satisfies(ex -> assertThat(((UpstreamServiceException) ex).getStatus()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT));
    }

    @Test
    void fetchVolumeById_timeout_throwsGatewayTimeout() {
        server.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE));

        assertThatThrownBy(() -> googleBookService.fetchVolumeById("slow"))
                .isInstanceOf(UpstreamServiceException.class)
                .hasMessageContaining("timeout")
                .satisfies(ex -> assertThat(((UpstreamServiceException) ex).getStatus()).isEqualTo(HttpStatus.GATEWAY_TIMEOUT));
    }

    @Test
    void fetchVolumeById_success() {
        String body = "{" +
                "\"id\":\"lRtdEAAAQBAJ\"," +
                "\"volumeInfo\":{\"title\":\"Effective Java\",\"authors\":[\"Joshua Bloch\"],\"pageCount\":416}" +
                "}";
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(body));

        GoogleBookResponse result = googleBookService.fetchVolumeById("lRtdEAAAQBAJ");
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("lRtdEAAAQBAJ");
        assertThat(result.volumeInfo().title()).isEqualTo("Effective Java");
    }
}
