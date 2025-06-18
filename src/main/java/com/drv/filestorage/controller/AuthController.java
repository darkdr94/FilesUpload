package com.drv.filestorage.controller;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtService jwtService;
    private final AuthenticationManager authManager;

    /**
     * Servicio que autentica a un usuario y retorna un token JWT
     * @param username
     * @param password
     * @return TokenJWT
     */
    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@RequestParam String username, @RequestParam String password) {
        log.info("Iniciando autenticacion del usuario:{}", username);
        var auth = new UsernamePasswordAuthenticationToken(username, password);
        authManager.authenticate(auth);
        log.info("Usuario {} autenticado exitosamente", username);
        return ResponseEntity.ok(jwtService.generateToken(username));
    }
}