package com.example.attendance.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.attendance.audit.AuditService;
import com.example.attendance.auth.dto.LoginRequest;
import com.example.attendance.auth.dto.LoginResponse;
import com.example.attendance.common.security.JwtService;
import com.example.attendance.user.AppUser;
import com.example.attendance.user.Role;
import com.example.attendance.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtService jwtService;
    @Mock
    UserRepository userRepository;
    @Mock
    AuditService auditService;
    @InjectMocks
    AuthService authService;

    @Test
    void loginSuccessReturnsJwtAndUser() {
        ReflectionTestUtils.setField(authService, "expirationMinutes", 30L);
        AppUser user = user(1L, "admin@example.com", Role.ADMIN);
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generate(user)).thenReturn("jwt-token");

        LoginResponse response = authService.login(new LoginRequest("admin@example.com", "Admin@12345"), "127.0.0.1");

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.user().email()).isEqualTo("admin@example.com");
        verify(auditService).log(user, "LOGIN_SUCCESS", "USER", 1L, "Login succeeded", "127.0.0.1");
    }

    @Test
    void invalidLoginIsRejectedAndAudited() {
        doThrow(new BadCredentialsException("bad")).when(authenticationManager)
            .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThatThrownBy(() -> authService.login(new LoginRequest("bad@example.com", "wrong"), "127.0.0.1"))
            .isInstanceOf(BadCredentialsException.class);

        verify(auditService).log(eq(null), eq("LOGIN_FAILURE"), eq("USER"), eq(null), eq("Login failed for email bad@example.com"), eq("127.0.0.1"));
    }

    private AppUser user(Long id, String email, Role role) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setName("Test User");
        user.setEmail(email);
        user.setRole(role);
        user.setPasswordHash("hash");
        return user;
    }
}
