package com.chatp2p.centralserver.dtos;

import io.swagger.v3.oas.annotations.media.Schema;

public class UpdateUserDTO {
    @Schema(description = "New username (optional)", example = "john_doe")
    private String username;

    @Schema(description = "New password (optional)", example = "newpassword123")
    private String password;

    @Schema(description = "New profile image URL (optional)", example = "/com/chatp2p/images/default_user.png")
    private String profileImageUrl;

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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}