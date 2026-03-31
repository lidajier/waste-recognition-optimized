package com.wasteai.service;

import com.wasteai.auth.AuthContext;
import com.wasteai.auth.JwtService;
import com.wasteai.auth.PasswordService;
import com.wasteai.domain.UserEntity;
import com.wasteai.dto.AuthRequest;
import com.wasteai.dto.AuthResponse;
import com.wasteai.dto.RegisterRequest;
import com.wasteai.dto.UserDto;
import com.wasteai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordService passwordService, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.username().trim();
        String email = request.email().trim().toLowerCase();
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists.");
        }

        String salt = passwordService.generateSalt();
        UserEntity user = new UserEntity();
        user.setUsername(username);
        user.setEmail(email);
        user.setDisplayName(request.displayName().trim());
        user.setRole("USER");
        user.setPasswordSalt(salt);
        user.setPasswordHash(passwordService.hash(request.password(), salt));
        user.setCreatedAt(LocalDateTime.now());

        UserEntity saved = userRepository.save(user);
        return new AuthResponse(jwtService.createToken(saved), toDto(saved));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(AuthRequest request) {
        UserEntity user = userRepository.findByUsername(request.username().trim())
                .or(() -> userRepository.findByEmail(request.username().trim().toLowerCase()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

        if (!passwordService.matches(request.password(), user.getPasswordSalt(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        return new AuthResponse(jwtService.createToken(user), toDto(user));
    }

    @Transactional(readOnly = true)
    public UserDto me() {
        return toDto(requireCurrentUser());
    }

    public UserEntity requireCurrentUser() {
        UserEntity user = AuthContext.get();
        if (user == null) {
            throw new IllegalStateException("Unauthorized.");
        }
        return user;
    }

    private UserDto toDto(UserEntity user) {
        return new UserDto(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}
