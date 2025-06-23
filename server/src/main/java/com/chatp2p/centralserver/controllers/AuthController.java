package com.chatp2p.centralserver.controllers;

import com.chatp2p.centralserver.dtos.CreateUserDTO;
import com.chatp2p.centralserver.dtos.LoginRequest;
import com.chatp2p.centralserver.exceptions.AuthException;
import com.chatp2p.centralserver.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody CreateUserDTO dto) {
        try {
            userService.registerUser(dto);
            return ResponseEntity.ok(Map.of("message", "User registered"));
        } catch (AuthException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }


    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(userService.loginUser(request));
        } catch (AuthException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }


    }

    @PostMapping("/logout")
    public ResponseEntity<Object> logout(@RequestHeader("Authorization") String token) {
        try {
            userService.logoutUser(token.replace("Bearer ", ""));
            return ResponseEntity.ok(Map.of("message", "Logout successful"));
        } catch (AuthException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }
}