package com.chatp2p.centralserver.controllers;

import com.chatp2p.centralserver.dtos.CreateUserDTO;
import com.chatp2p.centralserver.dtos.LoginRequest;
import com.chatp2p.centralserver.exceptions.AuthException;
import com.chatp2p.centralserver.services.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user authentication and registration")
public class AuthController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Register a new user", description = "Creates a new user account with username and password.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Registration error (e.g., username already taken)")
    })
    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody CreateUserDTO dto) {
        try {
            userService.registerUser(dto);
            return ResponseEntity.ok(Map.of("message", "User registered"));
        } catch (AuthException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Login user", description = "Authenticates a user and returns a JWT token.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login successful, returns user info and token"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {
        try {
            return ResponseEntity.ok(userService.loginUser(request));
        } catch (AuthException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Logout user", description = "Logs out the authenticated user.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired token")
    })
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