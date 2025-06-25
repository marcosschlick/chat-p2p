package com.chatp2p.centralserver.dtos;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

public class CreateUserDTO {
    @Schema(description = "Username for the new user", example = "john_doe")
    @NotBlank(message = "Username is required")
    private String username;

    @Schema(description = "Password for the new user", example = "mypassword123")
    @NotBlank(message = "Password is required")
    private String password;

    @Schema(description = "Profile image URL (optional)", example = "/com/chatp2p/images/default_user.png")
    private String profileImageUrl;

    public CreateUserDTO(String username, String password, String profileImageUrl) {
        this.username = username;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
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

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
