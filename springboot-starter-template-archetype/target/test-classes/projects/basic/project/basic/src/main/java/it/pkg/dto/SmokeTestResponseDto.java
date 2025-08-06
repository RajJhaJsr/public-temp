package it.pkg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.pkg.client.http.servicea.ExternalApiResponseDto;

import java.time.LocalDateTime;

public record SmokeTestResponseDto(
    @JsonProperty("id")
    Long id,
    
    @JsonProperty("name")
    String name,
    
    @JsonProperty("description")
    String description,
    
    @JsonProperty("status")
    String status,
    
    @JsonProperty("external_data")
    ExternalApiResponseDto externalData,
    
    @JsonProperty("created_at")
    LocalDateTime createdAt,
    
    @JsonProperty("message")
    String message
) {}