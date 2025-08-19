package com.bash.authproject.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    @Value("${jwt.secret.key}") // Injects the secret key from application properties
    private String jwtSecretKey;

    @Value("${jwt.access.token.expiration.time}") // Injects access token expiration time
    private long jwtAccessTokenExpirationTime;


    @Value("${jwt.refresh.token.expiration.time}") // Injects refresh token expiration time
    private long jwtRefreshTokenExpirationTime;

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Generates an access token with the configured expiration time
    public String generateAccessToken(UserPrincipal userPrincipal) {
        return generateToken(new HashMap<>(), userPrincipal, jwtAccessTokenExpirationTime);
    }

    // Generates an refresh token with the configured expiration time
    public String generateRefreshToken(UserPrincipal userPrincipal) {
        return generateToken(new HashMap<>(), userPrincipal, jwtRefreshTokenExpirationTime);
    }

    public String generateToken(Map<String, Object> extraClaims, UserPrincipal userPrincipal, long expirationTime) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userPrincipal.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean isTokenValid(String token, UserPrincipal userPrincipal) {
        final String username = extractUsername(token);
        return (username.equals(userPrincipal.getUsername()) && !isTokenExpired(token));
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parser()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    private Key getSignInKey() {
        byte[] keybytes = Decoders.BASE64.decode(jwtSecretKey);
        return Keys.hmacShaKeyFor(keybytes);
    }
}
