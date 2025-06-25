package com.chatp2p.centralserver.services;

import com.chatp2p.centralserver.config.JwtUtil;
import com.chatp2p.centralserver.dtos.CreateUserDTO;
import com.chatp2p.centralserver.dtos.LoginRequest;
import com.chatp2p.centralserver.dtos.LoginResponse;
import com.chatp2p.centralserver.dtos.UpdateUserDTO;
import com.chatp2p.centralserver.entities.User;
import com.chatp2p.centralserver.exceptions.AuthException;
import com.chatp2p.centralserver.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public void registerUser(CreateUserDTO dto) throws AuthException {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new AuthException("Username already exists");
        }
        String defaultImage = "/com/chatp2p/images/default_user.png";
        User user = new User(dto.getUsername(),
                passwordEncoder.encode(dto.getPassword()),
                defaultImage);
        userRepository.save(user);
    }

    @Transactional
    public LoginResponse loginUser(LoginRequest request) throws AuthException {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new AuthException("User not found"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new AuthException("Invalid password");
        }
        if (user.getOnline()) {
            throw new AuthException("User already online");
        }
        userRepository.updateOnlineStatusAndIp(user.getId(), true, request.getIp());
        String token = jwtUtil.generateToken(user.getId());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getProfileImageUrl());
    }

    @Transactional
    public void logoutUser(String token) throws AuthException {
        try {
            Long userId = jwtUtil.getUserIdFromToken(token);
            userRepository.updateOnlineStatus(userId, false);
        } catch (Exception e) {
            throw new AuthException("Invalid token");
        }
    }

    @Transactional
    public void updateUser(Long userId, UpdateUserDTO updateUserDTO) throws AuthException {
        User user = userRepository.findById(userId).orElseThrow(() -> new AuthException("User not found"));
        if (updateUserDTO.getUsername() != null && !updateUserDTO.getUsername().isBlank()) {
            if (userRepository.existsByUsernameAndIdNot(updateUserDTO.getUsername(), userId)) {
                throw new AuthException("Username already taken");
            }
            user.setUsername(updateUserDTO.getUsername());
        }
        if (updateUserDTO.getPassword() != null && !updateUserDTO.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(updateUserDTO.getPassword()));
        }
        if (updateUserDTO.getProfileImageUrl() != null) {
            String imagePath = "/com/chatp2p/images/" + updateUserDTO.getProfileImageUrl();
            user.setProfileImageUrl(imagePath);
        }
        userRepository.save(user);
    }

    public String findProfileImageByUsername(String username) throws AuthException {
        return userRepository.findProfileImageUrlByUsername(username)
                .orElseThrow(() -> new AuthException("User not found"));
    }
}