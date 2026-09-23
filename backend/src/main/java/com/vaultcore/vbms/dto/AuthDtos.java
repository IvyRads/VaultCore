package com.vaultcore.vbms.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public final class AuthDtos {
    private AuthDtos() { }
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) { }
    public record RegisterRequest(@NotBlank @Email String email, @NotBlank @Size(min=8) String password,
        @NotBlank String firstName, @NotBlank String lastName, @NotBlank String phone,
        @Past LocalDate dob, @NotBlank String gender, @NotBlank String address) { }
    public record LoginResponse(String token, Integer userId, String email, String role) { }
}
