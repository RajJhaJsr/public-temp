package it.pkg.client.http.servicea;

import com.fasterxml.jackson.annotation.JsonProperty;

// NOTE: This should come from OpenAPI generated lib containing models.
public record ExternalApiResponseDto(
        @JsonProperty("id")
        Integer id,

        @JsonProperty("userId")
        Integer userId,

        @JsonProperty("title")
        String title,

        @JsonProperty("body")
        String body
) {
}