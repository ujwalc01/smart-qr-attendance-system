package com.example.attendance.auth;

import com.example.attendance.audit.AuditService;
import com.example.attendance.auth.dto.LoginRequest;
import com.example.attendance.auth.dto.LoginResponse;
import com.example.attendance.common.security.JwtService;
import com.example.attendance.user.AppUser;
import com.example.attendance.user.UserRepository;
import com.example.attendance.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Value("${app.jwt.expiration-minutes}")
    private long expirationMinutes;

    public LoginResponse login(LoginRequest request, String ipAddress) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
            AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
            auditService.log(user, "LOGIN_SUCCESS", "USER", user.getId(), "Login succeeded", ipAddress);
            return new LoginResponse(jwtService.generate(user), expirationMinutes, UserResponse.from(user));
        } catch (AuthenticationException ex) {
            auditService.log(null, "LOGIN_FAILURE", "USER", null, "Login failed for email " + request.email(), ipAddress);
            throw new BadCredentialsException("Invalid email or password");
        }
    }
}
