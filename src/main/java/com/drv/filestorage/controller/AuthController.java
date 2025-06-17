package com.drv.filestorage.controller;

import com.drv.filestorage.common.dto.JwtResponseDto;
import com.drv.filestorage.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@RequestParam String username, @RequestParam String password) {
        var auth = new UsernamePasswordAuthenticationToken(username, password);
        authManager.authenticate(auth);
        return ResponseEntity.ok(jwtService.generateToken(username));
    }
}