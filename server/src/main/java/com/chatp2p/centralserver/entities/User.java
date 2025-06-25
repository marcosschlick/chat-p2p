package com.chatp2p.centralserver.entities;

import jakarta.persistence.*;
import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "User ID", example = "1")
    private Long id;

    @Column(unique = true)
    @Schema(description = "Unique username", example = "john_doe")
    private String username;

    @Column(name = "password_hash")
    @Schema(description = "Hashed password (internal use only)", example = "$2a$10$...")
    private String passwordHash;

    @Schema(description = "Online status", example = "true")
    private Boolean online = false;

    @Column(name = "profile_image_url")
    @Schema(description = "Profile image URL", example = "/com/chatp2p/images/default_user.png")
    private String profileImageUrl;

    @Column(name = "last_known_ip")
    @Schema(description = "Last known IP address", example = "192.168.0.10")
    private String lastKnownIp;

    public User() {
    }

    public User(String username, String passwordHash, String profileImageUrl) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.profileImageUrl = profileImageUrl;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getLastKnownIp() {
        return lastKnownIp;
    }

    public void setLastKnownIp(String lastKnownIp) {
        this.lastKnownIp = lastKnownIp;
    }
}
