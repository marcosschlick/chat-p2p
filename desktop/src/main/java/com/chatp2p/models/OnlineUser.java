package com.chatp2p.models;

public class OnlineUser {
    private String username;
    private String ip;
    private String profileImageUrl;

    public OnlineUser(String username, String ip, String profileImageUrl) {
        this.username = username;
        this.ip = ip;
        this.profileImageUrl = profileImageUrl;

    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
