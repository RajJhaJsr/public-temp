package it.pkg.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SmokeTestRequestDto(
    @JsonProperty("name")
    @NotBlank(message = "Name cannot be blank")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    String name,
    
    @JsonProperty("description")
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    String description
) {
}