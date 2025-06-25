package com.chatp2p.centralserver.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

public class LoginResponse {
    @Schema(description = "JWT token for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6...")
    private String token;

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "Username", example = "WilliamPalhares")
    private String username;

    @Schema(description = "Profile image URL", example = "default_user.png")
    private String profileImageUrl;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long id, String username, String profileImageUrl) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.profileImageUrl = profileImageUrl;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}