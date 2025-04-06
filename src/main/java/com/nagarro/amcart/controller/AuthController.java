package com.nagarro.amcart.controller;

import com.nagarro.amcart.dto.request.LoginRequest;
import com.nagarro.amcart.dto.request.SignupRequest;
import com.nagarro.amcart.dto.response.UserResponse;
import com.nagarro.amcart.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        UserResponse userResponse = authService.login(loginRequest);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody SignupRequest signupRequest) {
        UserResponse userResponse = authService.register(signupRequest);
        
        // Create a response with a message and the user data (without token)
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Registration successful! Please login.");
        response.put("user", userResponse);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestParam String email) {
        authService.initiatePasswordReset(email);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password reset initiated. Check your email for verification code.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
            @RequestParam String email,
            @RequestParam String code,
            @RequestParam String newPassword) {
        authService.completePasswordReset(email, code, newPassword);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Password has been reset successfully.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token.replace("Bearer ", ""));
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logged out successfully");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current-user")
    public ResponseEntity<UserResponse> getCurrentUser() {
        UserResponse userResponse = authService.getCurrentUser();
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/social-login")
    public ResponseEntity<UserResponse> socialLogin(@RequestParam String idToken, @RequestParam String provider) {
        UserResponse userResponse = authService.handleSocialLogin(idToken, provider);
        return ResponseEntity.ok(userResponse);
    }
}