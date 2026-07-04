package com.leaveflow.backend.service;


import com.leaveflow.backend.dto.LoginRequest;
import com.leaveflow.backend.dto.LoginResponse;
import com.leaveflow.backend.dto.RefreshTokenRequest;
import com.leaveflow.backend.entity.Employee;
import com.leaveflow.backend.exception.UnauthorizedException;
import com.leaveflow.backend.repository.EmployeeRepository;
import com.leaveflow.backend.exception.BadRequestException;
import com.leaveflow.backend.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final EmployeeRepository employeeRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        Employee employee = employeeRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> {
                    log.warn("Login failed: No account found for email: {}", request.getEmail());
                    return new BadRequestException("Invalid email or password.");
                });

        if (!passwordEncoder.matches(request.getPassword(), employee.getPasswordHash())) {
            log.warn("Login failed: Invalid password for email: {}", request.getEmail());
            throw new BadRequestException("Invalid email or password.");
        }

        String accessToken = jwtUtil.generateAccessToken(
                employee.getId(),
                employee.getEmail(),
                employee.getRole().name()
        );

        String refreshToken = jwtUtil.generateRefreshToken(
                employee.getId(),
                employee.getEmail(),
                employee.getRole().name()
        );

        log.info("Login successful for email: {}", request.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .id(employee.getId())
                .name(employee.getName())
                .email(employee.getEmail())
                .role(employee.getRole().name())
                .build();
    }

    public LoginResponse refreshAccessToken(RefreshTokenRequest request) {
        log.info("Attempting to refresh access token");

        String token = request.getRefreshToken();
        try {
            String email = jwtUtil.extractEmail(token);
            Long userId = jwtUtil.extractUserId(token);
            String role = jwtUtil.extractRole(token);

            if (!jwtUtil.isTokenValid(token, email)) {
                throw new UnauthorizedException("Refresh token is invalid or expired.");
            }

            String newAccessToken = jwtUtil.generateAccessToken(userId, email, role);

            Employee employee = employeeRepository.findById(userId)
                    .orElseThrow(() -> new BadRequestException("Employee not found."));

            return LoginResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(token)
                    .tokenType("Bearer")
                    .id(employee.getId())
                    .name(employee.getName())
                    .email(employee.getEmail())
                    .role(employee.getRole().name())
                    .build();
        } catch (Exception ex) {
            log.warn("Token refresh failed: {}", ex.getMessage());
            throw new UnauthorizedException("Refresh token is invalid or expired.");
        }
    }
}