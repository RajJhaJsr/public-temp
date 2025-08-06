package it.pkg;

import it.pkg.dto.SmokeTestRequestDto;
import it.pkg.dto.SmokeTestResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SmokeTestIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testSmokeTestEndpoint() {
        SmokeTestRequestDto request = new SmokeTestRequestDto(
            "Integration Test", 
            "Testing the smoke test endpoint"
        );

        webTestClient.post()
                .uri("/api/smoketest")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-API-Version", "v1")
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(SmokeTestResponseDto.class)
                .value(response -> {
                    assert response.name().equals("Integration Test");
                    assert response.message().equals("Operation completed successfully");
                    assert response.externalData() != null;
                });
    }
}