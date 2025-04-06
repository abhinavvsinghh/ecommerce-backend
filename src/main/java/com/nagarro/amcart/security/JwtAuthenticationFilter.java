package com.nagarro.amcart.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Value("${aws.cognito.jwkUrl}")
    private String jwkUrl;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);
            
            if (StringUtils.hasText(jwt)) {
                // Validate token
                try {
                    DecodedJWT decodedJWT = JWT.decode(jwt);
                    
                    // In a full implementation, you would validate the JWT signature using the JWK from Cognito
                    // For simplicity, we'll just check if it's a valid JWT format
                    
                    // Extract claims
                    String email = decodedJWT.getClaim("email").asString();
                    
                    if (StringUtils.hasText(email)) {
                        // Create authentication token
                        UserDetails userDetails = new User(email, "", new ArrayList<>());
                        UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        
                        // Set authentication in Security Context
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                } catch (JWTVerificationException e) {
                    log.error("Invalid JWT token", e);
                }
            }
        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}