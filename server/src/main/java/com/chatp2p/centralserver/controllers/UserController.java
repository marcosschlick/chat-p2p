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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Endpoints for user management and profile")
public class UserController {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Operation(summary = "Get online users", description = "Returns a list of online users with username, profile image URL, and IP address.")
    @ApiResponse(responseCode = "200", description = "List of online users")
    @GetMapping("/online")
    public ResponseEntity<List<Map<String, String>>> getOnlineUsers() {
        List<Map<String, String>> users = userRepository.findOnlineUsers().stream().map(user -> {
            Map<String, String> userData = new HashMap<>();
            userData.put("username", user.getUsername());
            userData.put("profileImageUrl", user.getProfileImageUrl());
            userData.put("ip", user.getLastKnownIp());
            return userData;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Get user by username", description = "Returns user information including ID for a given username.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User found"), @ApiResponse(responseCode = "404", description = "User not found")})
    @GetMapping("/by-username/{username}")
    public ResponseEntity<Map<String, Object>> getUserByUsername(@PathVariable String username) {
        try {
            return userRepository.findByUsername(username).map(user -> {
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("username", user.getUsername());
                userData.put("profileImageUrl", user.getProfileImageUrl());
                return ResponseEntity.ok(userData);
            }).orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Usuário não encontrado")));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Erro interno do servidor"));
        }
    }

    @Operation(summary = "Update user profile", description = "Updates the authenticated user's profile information (username, password, profile image). Requires JWT token.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "User updated successfully"), @ApiResponse(responseCode = "400", description = "Invalid data or username already taken"), @ApiResponse(responseCode = "401", description = "Invalid or expired token")})
    @PutMapping("/me")
    public ResponseEntity<Object> updateUser(@RequestHeader("Authorization") String authHeader, @RequestBody UpdateUserDTO updateUserDTO) {
        try {
            String token = authHeader.replace("Bearer ", "");
            Long userId = jwtUtil.getUserIdFromToken(token);
            System.out.println(updateUserDTO.getUsername());
            System.out.println(updateUserDTO.getProfileImageUrl());
            userService.updateUser(userId, updateUserDTO);
            return ResponseEntity.ok(Map.of("message", "User updated successfully"));
        } catch (AuthException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid token"));
        }
    }

    @Operation(summary = "Get profile image by username", description = "Returns the profile image URL for a given username.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Profile image URL found"), @ApiResponse(responseCode = "404", description = "User not found")})
    @GetMapping("/profile-image/{username}")
    public ResponseEntity<Object> getProfileImageByUsername(@PathVariable String username) {
        try {
            String imageUrl = userService.findProfileImageByUsername(username);
            return ResponseEntity.ok(Map.of("profileImageUrl", imageUrl));
        } catch (AuthException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }
}