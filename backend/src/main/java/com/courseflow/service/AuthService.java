package com.courseflow.service;

import com.courseflow.dto.request.LoginRequest;
import com.courseflow.dto.request.RegisterRequest;
import com.courseflow.dto.response.AuthResponse;
import com.courseflow.dto.response.RegisterResponse;
import com.courseflow.exception.ConflictException;
import com.courseflow.model.User;
import com.courseflow.model.UserRole;
import com.courseflow.repository.UserRepository;
import com.courseflow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final com.courseflow.security.UserDetailsServiceImpl userDetailsService;
    private final JwtUtil jwtUtil;

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ConflictException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email is already registered");
        }

        if (userRepository.existsByStudentId(request.getStudentId())) {
            throw new ConflictException("Student ID is already registered");
        }

        User user = User.builder()
            .fullName(request.getFullName().trim())
            .email(request.getEmail().trim().toLowerCase())
            .password(passwordEncoder.encode(request.getPassword()))
            .role(UserRole.STUDENT)
            .studentId(request.getStudentId().trim())
            .department(request.getDepartment().trim())
            .phone(request.getPhone().trim())
            .semester(request.getSemester())
            .maxCredits(24)
            .isActive(true)
            .build();

        User saved = userRepository.save(user);
        return new RegisterResponse("Account created successfully", saved.getId());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail().trim().toLowerCase(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
            .orElseThrow(() -> new org.springframework.security.authentication.BadCredentialsException("Invalid email or password"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(user);
        if (!jwtUtil.isTokenValid(token, userDetails)) {
            throw new org.springframework.security.authentication.BadCredentialsException("Token generation failed");
        }

        return AuthResponse.builder()
            .token(token)
            .tokenType("Bearer")
            .userId(user.getId())
            .email(user.getEmail())
            .fullName(user.getFullName())
            .role(user.getRole())
            .department(user.getDepartment())
            .semester(user.getSemester())
            .maxCredits(user.getMaxCredits())
            .build();
    }
}
