package com.chatp2p.centralserver.controllers;

import com.chatp2p.centralserver.config.JwtUtil;
import com.chatp2p.centralserver.dtos.UpdateUserDTO;
import com.chatp2p.centralserver.exceptions.AuthException;
import com.chatp2p.centralserver.repositories.UserRepository;
import com.chatp2p.centralserver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

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

    @PutMapping("/me")
    public ResponseEntity<Object> updateUser(@RequestHeader("Authorization") String authHeader, @RequestBody UpdateUserDTO updateUserDTO) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);

            userService.updateUser(userId, updateUserDTO);
            return ResponseEntity.ok(Map.of("message", "User updated successfully"));
        } catch (AuthException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }
    }
}