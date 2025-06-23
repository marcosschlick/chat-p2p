package com.chatp2p.centralserver.controllers;

import com.chatp2p.centralserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/online")
    public ResponseEntity<List<Map<String, String>>> getOnlineUsers() {
        List<Map<String, String>> users = userRepository.findOnlineUsers().stream().map(user -> {
            Map<String, String> userData = new HashMap<>();
            userData.put("username", user.getUsername());
            userData.put("profileImageUrl", user.getProfileImageUrl());
            userData.put("ip", user.getLastKnownIp()); // Novo campo
            return userData;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }
}