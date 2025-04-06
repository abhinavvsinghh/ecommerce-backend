package com.nagarro.amcart.service.impl;

import com.amazonaws.services.cognitoidp.AWSCognitoIdentityProvider;
import com.amazonaws.services.cognitoidp.model.*;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nagarro.amcart.dto.request.LoginRequest;
import com.nagarro.amcart.dto.request.SignupRequest;
import com.nagarro.amcart.dto.response.UserResponse;
import com.nagarro.amcart.exception.ResourceNotFoundException;
import com.nagarro.amcart.exception.UnauthorizedException;
import com.nagarro.amcart.model.User;
import com.nagarro.amcart.repository.UserRepository;
import com.nagarro.amcart.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AWSCognitoIdentityProvider cognitoClient;

    @Value("${aws.region}")
    private String region;

    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.cognito.clientId}")
    private String clientId;

    @Override
    public UserResponse login(LoginRequest loginRequest) {
        try {
            // AWS Cognito authentication
            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", loginRequest.getEmail());
            authParams.put("PASSWORD", loginRequest.getPassword());

            AdminInitiateAuthRequest authRequest = new AdminInitiateAuthRequest()
                    .withAuthFlow("ADMIN_NO_SRP_AUTH")
                    .withClientId(clientId)
                    .withUserPoolId(userPoolId)
                    .withAuthParameters(authParams);

            AdminInitiateAuthResult authResult = cognitoClient.adminInitiateAuth(authRequest);
            AuthenticationResultType resultType = authResult.getAuthenticationResult();
            
            // Get user from our database
            User user = userRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new ResourceNotFoundException("User", "email", loginRequest.getEmail()));

            // Build response
            return UserResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .mobileNumber(user.getMobileNumber())
                    .token(resultType.getIdToken())
                    .build();
        } catch (UserNotFoundException ex) {
            throw new ResourceNotFoundException("User not found with email: " + loginRequest.getEmail());
        } catch (NotAuthorizedException ex) {
            throw new UnauthorizedException("Invalid credentials");
        } catch (Exception ex) {
            log.error("Error during login", ex);
            throw new UnauthorizedException("Authentication failed");
        }
    }

    @Override
    @Transactional
    public UserResponse register(SignupRequest signupRequest) {
        if (userRepository.existsByEmail(signupRequest.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        try {
            // First check if the user already exists in Cognito
            try {
                AdminGetUserRequest getUserRequest = new AdminGetUserRequest()
                    .withUserPoolId(userPoolId)
                    .withUsername(signupRequest.getEmail());
                
                cognitoClient.adminGetUser(getUserRequest);
                
                // If we get here, the user exists in Cognito - let's delete them first
                log.info("User exists in Cognito but not in our database. Deleting from Cognito: {}", signupRequest.getEmail());
                
                AdminDeleteUserRequest deleteUserRequest = new AdminDeleteUserRequest()
                    .withUserPoolId(userPoolId)
                    .withUsername(signupRequest.getEmail());
                
                cognitoClient.adminDeleteUser(deleteUserRequest);
                
                // Small delay to ensure deletion is complete
                Thread.sleep(1000);
            } catch (UserNotFoundException ex) {
                // This is actually good - user doesn't exist in Cognito
                log.info("User doesn't exist in Cognito. Proceeding with registration: {}", signupRequest.getEmail());
            }
        
            // Register with AWS Cognito
            AttributeType emailAttr = new AttributeType()
                    .withName("email")
                    .withValue(signupRequest.getEmail());

            AttributeType firstNameAttr = new AttributeType()
                    .withName("given_name")
                    .withValue(signupRequest.getFirstName());
                    
            AttributeType lastNameAttr = new AttributeType()
                    .withName("family_name")
                    .withValue(signupRequest.getLastName());

            AttributeType phoneAttr = new AttributeType()
                    .withName("phone_number")
                    .withValue("+91"+signupRequest.getMobileNumber());

            // Use email as username since that's what Cognito requires in this setup
            AdminCreateUserRequest createUserRequest = new AdminCreateUserRequest()
                    .withUserPoolId(userPoolId)
                    .withUsername(signupRequest.getEmail())
                    .withTemporaryPassword(signupRequest.getPassword())
                    .withUserAttributes(emailAttr, firstNameAttr, lastNameAttr, phoneAttr)
                    .withMessageAction("SUPPRESS"); // Suppress sending email since we're setting the password directly

            AdminCreateUserResult createUserResult = cognitoClient.adminCreateUser(createUserRequest);
            String cognitoUserId = createUserResult.getUser().getUsername();

            // Set permanent password (skip temporary password flow)
            AdminSetUserPasswordRequest passwordRequest = new AdminSetUserPasswordRequest()
                    .withUserPoolId(userPoolId)
                    .withUsername(signupRequest.getEmail())
                    .withPassword(signupRequest.getPassword())
                    .withPermanent(true);

            cognitoClient.adminSetUserPassword(passwordRequest);

            // Create user in our database
            User user = User.builder()
                    .email(signupRequest.getEmail())
                    .firstName(signupRequest.getFirstName())
                    .lastName(signupRequest.getLastName())
                    .mobileNumber(signupRequest.getMobileNumber())
                    .cognitoId(cognitoUserId)
                    .createdAt(new Date())
                    .updatedAt(new Date())
                    .active(true)
                    .build();

            User savedUser = userRepository.save(user);
            
            log.info("User successfully registered: {}", signupRequest.getEmail());
            
            // Instead of auto-login, just return the user info without a token
            // The frontend can redirect to login page
            return UserResponse.builder()
                    .id(savedUser.getId())
                    .email(savedUser.getEmail())
                    .firstName(savedUser.getFirstName())
                    .lastName(savedUser.getLastName())
                    .mobileNumber(savedUser.getMobileNumber())
                    // No token included because we're not logging in automatically
                    .build();
            
        } catch (Exception ex) {
            log.error("Error during registration", ex);
            throw new RuntimeException("Registration failed: " + ex.getMessage());
        }
    }

    @Override
    public void logout(String token) {
        try {
            GlobalSignOutRequest signOutRequest = new GlobalSignOutRequest()
                    .withAccessToken(token);
            cognitoClient.globalSignOut(signOutRequest);
        } catch (Exception ex) {
            log.error("Error during logout", ex);
            throw new RuntimeException("Logout failed");
        }
    }

    @Override
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User not authenticated");
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .mobileNumber(user.getMobileNumber())
                .build();
    }

    @Override
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
    
    @Override
    public void initiatePasswordReset(String email) {
        try {
            // Since email is the username, we can directly use it
            ForgotPasswordRequest request = new ForgotPasswordRequest()
                    .withClientId(clientId)
                    .withUsername(email);
            
            cognitoClient.forgotPassword(request);
            log.info("Password reset initiated for user: {}", email);
        } catch (UserNotFoundException ex) {
            log.error("User not found during password reset: {}", email);
            throw new ResourceNotFoundException("User not found with email: " + email);
        } catch (Exception ex) {
            log.error("Error initiating password reset", ex);
            throw new RuntimeException("Failed to initiate password reset: " + ex.getMessage());
        }
    }

    @Override
    public void completePasswordReset(String email, String code, String newPassword) {
        try {
            // Since email is the username, we can directly use it
            ConfirmForgotPasswordRequest request = new ConfirmForgotPasswordRequest()
                    .withClientId(clientId)
                    .withUsername(email)
                    .withConfirmationCode(code)
                    .withPassword(newPassword);
            
            cognitoClient.confirmForgotPassword(request);
            log.info("Password reset completed for user: {}", email);
        } catch (CodeMismatchException ex) {
            log.error("Invalid verification code: {}", email);
            throw new IllegalArgumentException("Invalid verification code");
        } catch (Exception ex) {
            log.error("Error completing password reset", ex);
            throw new RuntimeException("Failed to complete password reset: " + ex.getMessage());
        }
    }

    @Override
    public UserResponse handleSocialLogin(String idToken, String provider) {
        try {
            // For Google login with Cognito federation
            Map<String, String> logins = new HashMap<>();
            // The key depends on the provider, for Google it would be cognito-idp.{region}.amazonaws.com/{userPoolId}
            String providerKey = "cognito-idp." + region + ".amazonaws.com/" + userPoolId;
            logins.put(providerKey, idToken);
            
            // Try to find the user by their email from the token
            // You'd need to decode the JWT token to get the email
            String email = getEmailFromIdToken(idToken);
            
            Optional<User> existingUser = userRepository.findByEmail(email);
            
            if (existingUser.isPresent()) {
                // User exists, return user info
                User user = existingUser.get();
                return createUserResponse(user, idToken);
            } else {
                // User doesn't exist, create a new one
                // Extract user info from the ID token
                Map<String, String> userInfo = extractUserInfoFromIdToken(idToken);
                
                User newUser = User.builder()
                        .email(email)
                        .firstName(userInfo.getOrDefault("given_name", ""))
                        .lastName(userInfo.getOrDefault("family_name", ""))
                        .mobileNumber("")  // Social login might not provide phone number
                        .createdAt(new Date())
                        .updatedAt(new Date())
                        .active(true)
                        .build();
                
                User savedUser = userRepository.save(newUser);
                return createUserResponse(savedUser, idToken);
            }
        } catch (Exception ex) {
            log.error("Error during social login", ex);
            throw new RuntimeException("Social login failed: " + ex.getMessage());
        }
    }
    

    private String getEmailFromIdToken(String idToken) {
        try {
            DecodedJWT jwt = JWT.decode(idToken);
            
            // Try "email" claim first (most common)
            String email = jwt.getClaim("email").asString();
            
            // If email is null, try other possible locations
            if (!StringUtils.hasText(email)) {
                // Check for "preferred_username" if it contains an email
                String preferredUsername = jwt.getClaim("preferred_username").asString();
                if (StringUtils.hasText(preferredUsername) && preferredUsername.contains("@")) {
                    email = preferredUsername;
                }
            }
            
            if (!StringUtils.hasText(email)) {
                // Try to find email in custom attributes
                email = jwt.getClaim("custom:email").asString();
            }
            
            if (!StringUtils.hasText(email)) {
                log.error("Email not found in ID token");
                throw new RuntimeException("Could not extract email from ID token");
            }
            
            return email;
        } catch (JWTDecodeException e) {
            log.error("Error decoding JWT token", e);
            throw new RuntimeException("Invalid ID token format");
        } catch (Exception e) {
            log.error("Error extracting email from ID token", e);
            throw new RuntimeException("Error processing ID token");
        }
    }
    
    private Map<String, String> extractUserInfoFromIdToken(String idToken) {
        try {
            DecodedJWT jwt = JWT.decode(idToken);
            Map<String, String> userInfo = new HashMap<>();
            
            // Extract common user attributes
            String givenName = jwt.getClaim("given_name").asString();
            if (StringUtils.hasText(givenName)) {
                userInfo.put("given_name", givenName);
            }
            
            String familyName = jwt.getClaim("family_name").asString();
            if (StringUtils.hasText(familyName)) {
                userInfo.put("family_name", familyName);
            }
            
            String name = jwt.getClaim("name").asString();
            if (StringUtils.hasText(name)) {
                userInfo.put("name", name);
                
                // If we don't have given/family name but have full name, use it
                if (!userInfo.containsKey("given_name") && !userInfo.containsKey("family_name")) {
                    String[] nameParts = name.split(" ", 2);
                    if (nameParts.length > 0) {
                        userInfo.put("given_name", nameParts[0]);
                        if (nameParts.length > 1) {
                            userInfo.put("family_name", nameParts[1]);
                        }
                    }
                }
            }
            
            // Get picture if available
            String picture = jwt.getClaim("picture").asString();
            if (StringUtils.hasText(picture)) {
                userInfo.put("picture", picture);
            }
            
            // Get locale if available
            String locale = jwt.getClaim("locale").asString();
            if (StringUtils.hasText(locale)) {
                userInfo.put("locale", locale);
            }
            
            // If given_name and family_name are still missing, set defaults
            if (!userInfo.containsKey("given_name")) {
                userInfo.put("given_name", "User");
            }
            
            if (!userInfo.containsKey("family_name")) {
                userInfo.put("family_name", String.valueOf(System.currentTimeMillis() % 10000)); // Use timestamp as placeholder
            }
            
            return userInfo;
        } catch (JWTDecodeException e) {
            log.error("Error decoding JWT token", e);
            throw new RuntimeException("Invalid ID token format");
        } catch (Exception e) {
            log.error("Error extracting user info from ID token", e);
            throw new RuntimeException("Error processing ID token");
        }
    }
    
    private UserResponse createUserResponse(User user, String token) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .mobileNumber(user.getMobileNumber())
                .token(token)
                .build();
    }
}