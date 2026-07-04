package com.leaveflow.backend.controller;

import com.leaveflow.backend.dto.LoginRequest;
import com.leaveflow.backend.dto.LoginResponse;
import com.leaveflow.backend.dto.RefreshTokenRequest;
import com.leaveflow.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints for login/logout and token management")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements()
    @Operation(summary = "User Login", description = "Authenticate user with email and password. Returns JWT access and refresh tokens.")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping("/refresh")
    @SecurityRequirements()
    @Operation(summary = "Refresh Access Token", description = "Generate a new access token using a valid refresh token.")
    public ResponseEntity<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshAccessToken(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "User Logout", description = "Logout the current user (stateless, just invalidate on client side).")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }
}
