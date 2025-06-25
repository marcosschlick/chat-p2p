package com.chatp2p.centralserver.dtos;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public class LoginRequest {
    @Schema(description = "Username for login", example = "MarcosSchlick")
    @NotBlank(message = "Username is required")
    private String username;
    @Schema(description = "Password for login", example = "euodeiojava")
    @NotBlank(message = "Password is required")
    private String password;
    @Schema(description = "User's IP address", example = "192.168.0.10")
    @NotBlank(message = "IP is required")
    private String ip;

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

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
}