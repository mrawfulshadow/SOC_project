package com.soc.authservice;

import com.soc.authservice.dto.AuthRequest;
import com.soc.authservice.dto.AuthResponse;
import com.soc.authservice.dto.RegisterRequest;
import com.soc.authservice.model.User;
import com.soc.authservice.repository.UserRepository;
import com.soc.authservice.service.AuthService;
import com.soc.authservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;
    private AuthRequest authRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest("testuser", "Password123!", "test@example.com", "ROLE_USER");
        authRequest = new AuthRequest("testuser", "Password123!");
        user = User.builder()
                .id("u1")
                .username("testuser")
                .password("encoded_Password123!")
                .email("test@example.com")
                .role("ROLE_USER")
                .build();
    }

    @Test
    void testRegister_Success() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Password123!")).thenReturn("encoded_Password123!");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateToken("testuser", "ROLE_USER")).thenReturn("mock-jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(86400000L);

        AuthResponse response = authService.register(registerRequest);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("ROLE_USER", response.getRole());
        assertEquals("mock-jwt-token", response.getToken());
        verify(passwordEncoder, times(1)).encode("Password123!");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateUsername_ThrowsException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        assertEquals("Username already exists", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsException() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        assertEquals("Email already in use", ex.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "encoded_Password123!")).thenReturn(true);
        when(jwtUtil.generateToken("testuser", "ROLE_USER")).thenReturn("mock-jwt-token");
        when(jwtUtil.getExpirationTime()).thenReturn(86400000L);

        AuthResponse response = authService.login(authRequest);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        assertEquals("mock-jwt-token", response.getToken());
    }

    @Test
    void testLogin_InvalidPassword_ThrowsException() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Password123!", "encoded_Password123!")).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(authRequest));
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void testLogin_UserNotFound_ThrowsException() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        AuthRequest unknownReq = new AuthRequest("unknown", "Password123!");
        RuntimeException ex = assertThrows(RuntimeException.class, () -> authService.login(unknownReq));
        assertEquals("Invalid username or password", ex.getMessage());
    }

    @Test
    void testJwtUtil_Lifecycle() {
        JwtUtil realJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(realJwtUtil, "secret", "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970");
        ReflectionTestUtils.setField(realJwtUtil, "jwtExpiration", 3600000L);

        String token = realJwtUtil.generateToken("admin_test", "ROLE_ADMIN");
        assertNotNull(token);
        assertTrue(realJwtUtil.validateToken(token));
        assertEquals("admin_test", realJwtUtil.getUsernameFromToken(token));
        assertEquals("ROLE_ADMIN", realJwtUtil.getRoleFromToken(token));
    }
}
