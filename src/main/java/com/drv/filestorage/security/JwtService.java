package com.drv.filestorage.security;

import com.drv.filestorage.common.dto.JwtResponseDto;
import com.drv.filestorage.exception.InvalidJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    @Value("${security.jwt.expiration-ms}")
    private long expirationInMs;

    private Key key;

    @PostConstruct
    public void init() {
        key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    //TODO almacenar variable de tiempo en ssm
    public JwtResponseDto generateToken(String username) {
        String tokenJwt = Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationInMs)) // 1 hora
                .signWith(key)
                .compact();
        return new JwtResponseDto(tokenJwt);
    }

    public String validateTokenAndGetUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (Exception ex) {
            throw new InvalidJwtException("Token JWT invalido o modificado.");
        }
    }
}
