package com.marcaaiback.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    public static final String JWT_BEARER = "Bearer ";
    public static final String JWT_AUTHORIZATION = "Authorization";

    public static final long EXPIRE_DAYS = 0;
    public static final long EXPIRE_HOURS = 1;
    public static final long EXPIRE_MINUTES = 30;

    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey generateKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    private Date dataDeExpiracao(Date start) {
        LocalDateTime dateTime = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        LocalDateTime end = dateTime.plusDays(EXPIRE_DAYS).plusHours(EXPIRE_HOURS).plusMinutes(EXPIRE_MINUTES);
        return Date.from(end.atZone(ZoneId.systemDefault()).toInstant());
    }

    public JwtToken gerarToken(Long id, String email) {
        Date issuedAt = new Date();
        Date expiration = dataDeExpiracao(issuedAt);

        String token = Jwts.builder()
                .subject(email)
                .claim("id", id)
                .issuedAt(issuedAt)
                .expiration(expiration)
                .signWith(generateKey())
                .compact();

        return new JwtToken(token);
    }

    private Claims obterReivindicacoesDoToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(generateKey())
                    .build()
                    .parseSignedClaims(refatorarToken(token))
                    .getPayload();
        } catch (JwtException ex) {
            log.error("Token inválido {}", ex.getMessage());
        }
        return null;
    }

    public String obterEmailDoToken(String token) {
        Claims claims = obterReivindicacoesDoToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    public boolean tokenValido(String token) {
        try {
            Jwts.parser()
                    .verifyWith(generateKey())
                    .build()
                    .parseSignedClaims(refatorarToken(token));
            return true;
        } catch (JwtException ex) {
            log.error("Token inválido. {}", ex.getMessage());
        }
        return false;
    }

    public String refatorarToken(String token) {
        return token.replace(JWT_BEARER, "");
    }
}