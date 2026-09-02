package com.example.intellierp.service;

import com.example.intellierp.dto.JwtResponse;
import com.example.intellierp.dto.LoginRequest;
import com.example.intellierp.dto.RegisterRequest;
import com.example.intellierp.dto.UserProfileDto;
import com.example.intellierp.entity.User;
import com.example.intellierp.entity.enums.RoleType;
import com.example.intellierp.exception.BadRequestException;
import com.example.intellierp.exception.ResourceNotFoundException;
import com.example.intellierp.repository.UserRepository;
import com.example.intellierp.security.UserDetailsImpl;
import com.example.intellierp.security.jwt.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuditLogService auditLogService;

    @Transactional
    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername());

        User user = userRepository.findById(userDetails.getId()).orElse(null);
        if (user != null) {
            auditLogService.logAction(user, "LOGIN", "User", user.getId(), "User logged in: " + user.getUsername(), null);
        }

        return new JwtResponse(
                jwt,
                refreshToken,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getFullName(),
                userDetails.getEmail(),
                userDetails.getRole()
        );
    }

    @Transactional
    public UserProfileDto registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username is already taken!");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already in use!");
        }

        User user = new User(
                registerRequest.getUsername(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getFullName(),
                registerRequest.getEmail(),
                registerRequest.getRole() != null ? registerRequest.getRole() : RoleType.ROLE_EMPLOYEE
        );

        User savedUser = userRepository.save(user);
        auditLogService.logAction(savedUser, "REGISTER", "User", savedUser.getId(), "New user registered: " + savedUser.getUsername(), null);
        return new UserProfileDto(savedUser);
    }

    public UserProfileDto getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            throw new ResourceNotFoundException("No authenticated user found");
        }
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return new UserProfileDto(user);
    }

    public JwtResponse refreshToken(String refreshToken) {
        if (refreshToken != null && jwtUtils.validateJwtToken(refreshToken)) {
            String username = jwtUtils.getUserNameFromJwtToken(refreshToken);
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found for refresh token: " + username));

            String newJwt = jwtUtils.generateTokenFromUsername(username, 86400000);
            return new JwtResponse(
                    newJwt,
                    refreshToken,
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail(),
                    user.getRole()
            );
        }
        throw new BadRequestException("Invalid or expired refresh token");
    }
}
