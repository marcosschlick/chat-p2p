package com.chatp2p.centralserver.dtos;

public class LoginResponse {
    private String token;
    private Long id;
    private String username;
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