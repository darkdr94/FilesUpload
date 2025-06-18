package com.drv.filestorage.controller;

import com.drv.filestorage.common.dto.JwtResponseDto;
import com.drv.filestorage.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthControllerTest {

    private JwtService jwtService;
    private AuthenticationManager authManager;
    private AuthController authController;

    @BeforeEach
    void setUp() {
        jwtService = mock(JwtService.class);
        authManager = mock(AuthenticationManager.class);
        authController = new AuthController(jwtService, authManager);
    }

    @Test
    void testLogin_successfulAuthentication_returnsJwt() {
        String username = "testuser";
        String password = "password";
        JwtResponseDto fakeToken = new JwtResponseDto("fake-jwt-token");

        when(jwtService.generateToken(username)).thenReturn(fakeToken);

        when(jwtService.generateToken(username)).thenReturn(fakeToken);

        when(authManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(Authentication.class));
        ResponseEntity<JwtResponseDto> response = authController.login(username, password);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("fake-jwt-token", response.getBody().getToken());

        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(username);
    }

    @Test
    void testLogin_invalidCredentials_throwsException() {
        String username = "baduser";
        String password = "wrongpass";

        doThrow(new BadCredentialsException("Invalid credentials"))
                .when(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        BadCredentialsException exception = assertThrows(
                BadCredentialsException.class,
                () -> authController.login(username, password)
        );

        assertEquals("Invalid credentials", exception.getMessage());
        verify(authManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(jwtService);
    }
}
