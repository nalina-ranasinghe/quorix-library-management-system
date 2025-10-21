package com.library.app.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 4, max = 50)
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "Username can contain letters, numbers, dot, underscore, hyphen")
    private String username;

    @NotBlank
    @Size(min = 2, max = 100)
    private String fullName;

    @NotBlank
    @Email
    private String email;

    // Phone is required (per your note)
    @NotBlank(message = "Phone number is required")
    @Size(min = 7, max = 20)
    private String phone;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Password must contain at least one letter and one number")
    private String password;

    @NotBlank
    private String confirmPassword;
}