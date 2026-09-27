package com.rikkeibank.identity.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevokeTokenRequest {
    @NotBlank(message = "Token or user email is required")
    private String targetEmail;
    private String specificToken;
    private String reason;
}
