package com.kynsof.identity.application.command.auth.autenticate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Request object for user authentication.
 * 
 * This class represents the data required for user login,
 * including username and password with appropriate validations.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor // Required for deserialization
@Builder
public class LoginRequest implements Serializable {
    @Serial
    private static final long serialVersionUID = -6587730290416571673L;
    
    /**
     * The username for authentication.
     * Must not be blank and have a length between 3 and 50 characters.
     */
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    /**
     * The user's password for authentication.
     * Must not be blank and have a minimum length of 8 characters.
     */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
