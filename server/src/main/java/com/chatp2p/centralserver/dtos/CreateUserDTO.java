package com.chatp2p.centralserver.dtos;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public class CreateUserDTO {
    @Schema(description = "Username for the new user", example = "MarcosSchlick")
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(description = "Password for the new user", example = "euodeiojava")
    @NotBlank(message = "Password is required")
    private String password;

    public CreateUserDTO(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
