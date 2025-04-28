package com.bufalari.receivable.security; // <<< PACOTE CORRETO

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${security.jwt.token.secret-key}") // <<< PROPRIEDADE CORRETA
    private String configuredSecretKey;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        if (configuredSecretKey == null || configuredSecretKey.isBlank()) {
            log.error("JWT secret key is not configured properly in application properties (security.jwt.token.secret-key).");
            throw new IllegalStateException("JWT secret key must be configured.");
        }
        try {
            this.secretKey = Keys.hmacShaKeyFor(configuredSecretKey.getBytes(StandardCharsets.UTF_8));
            log.info("JWT Secret Key initialized successfully for Accounts Receivable Service."); // Mensagem específica
        } catch (Exception e) {
            log.error("Error initializing JWT Secret Key from configured value.", e);
            throw new RuntimeException("Failed to initialize JWT Secret Key", e);
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) throws JwtException {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(this.secretKey)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            throw e;
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            throw e;
        } catch (MalformedJwtException e) {
            log.warn("JWT token is malformed: {}", e.getMessage());
            throw e;
        } catch (SignatureException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
            throw e;
        } catch (IllegalArgumentException e) {
            log.warn("JWT token argument validation failed: {}", e.getMessage());
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        } catch (JwtException e) {
            log.warn("Could not determine expiration due to other JWT exception: {}", e.getMessage());
            return true;
        }
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
         try {
             final String username = extractUsername(token);
             if (userDetails == null) {
                  log.warn("UserDetails object provided for validation is null for token subject: {}", username);
                  return false;
             }
             return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
         } catch (JwtException e) {
             return false;
         }
    }
     // Métodos de geração de token não são necessários aqui
}