package com.alumniconnect.backend.controller;

import com.alumniconnect.backend.dto.auth.*;
import com.alumniconnect.backend.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Login, student registration, Google sign-in, and the alumni claim-profile flow")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register/student")
    public ResponseEntity<AuthResponse> registerStudent(@Valid @RequestBody RegisterStudentRequest request) {
        return ResponseEntity.ok(authService.registerStudent(request));
    }

    @PostMapping("/claim/lookup")
    public ResponseEntity<ClaimLookupResponse> claimLookup(@Valid @RequestBody ClaimLookupRequest request) {
        return ResponseEntity.ok(authService.findClaimableRecord(request));
    }

    @PostMapping("/claim/confirm")
    public ResponseEntity<AuthResponse> claimConfirm(@Valid @RequestBody ClaimConfirmRequest request) {
        return ResponseEntity.ok(authService.claimProfile(request));
    }

    /** Public settings the sign-in page needs (whether to show the Google button, and with which client id). */
    @GetMapping("/config")
    public ResponseEntity<AuthConfigResponse> config() {
        return ResponseEntity.ok(authService.authConfig());
    }

    @PostMapping("/google")
    public ResponseEntity<GoogleLoginResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request));
    }

    @PostMapping("/google/register")
    public ResponseEntity<AuthResponse> googleRegister(@Valid @RequestBody GoogleRegisterRequest request) {
        return ResponseEntity.ok(authService.googleRegister(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal org.springframework.security.core.userdetails.UserDetails principal) {
        return ResponseEntity.ok(authService.getCurrentUser(principal.getUsername()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Stateless JWT — nothing to invalidate server-side in this milestone.
        // The frontend drops its token immediately regardless of this response.
        return ResponseEntity.ok().build();
    }
}
