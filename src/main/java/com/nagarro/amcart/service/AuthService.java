package com.nagarro.amcart.service;

import com.nagarro.amcart.dto.request.LoginRequest;
import com.nagarro.amcart.dto.request.SignupRequest;
import com.nagarro.amcart.dto.response.UserResponse;

public interface AuthService {

    UserResponse login(LoginRequest loginRequest);

    UserResponse register(SignupRequest signupRequest);

    void logout(String token);

    UserResponse getCurrentUser();

    boolean isAuthenticated();

    void initiatePasswordReset(String email);

    void completePasswordReset(String email, String code, String newPassword);

    // Social login
    UserResponse handleSocialLogin(String idToken, String provider);
}