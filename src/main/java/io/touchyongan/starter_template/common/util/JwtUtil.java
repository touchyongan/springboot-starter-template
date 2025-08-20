package io.touchyongan.starter_template.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.touchyongan.starter_template.feature.user.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private static final MessageDigest MESSAGE_DIGEST;
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder();
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    //private final RevokedJwtTokenRepository revokedJwtTokenRepository;

    static {
        try {
            MESSAGE_DIGEST = MessageDigest.getInstance("SHA-256");
        } catch (final NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Value("${auth.jwt.secret_key}")
    private String secretKey;
    @Value("${auth.jwt.expiration}")
    private long expiration;
    @Value("${auth.jwt.refresh_expiration}")
    private long refreshExpiration;

    public SecretKey getSecretKey() {
        final var keyBytes = Decoders.BASE64URL.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(final AppUser users) {
        return Jwts
                .builder()
                .claim("userId", users.getId())
                .claim("typ", "Bearer")
                .subject(users.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSecretKey()).compact();
    }

    public String generateMFAToken(final AppUser users) {
        return Jwts
                .builder()
                .claim("mfa", true)
                .subject(users.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + (2 * 60 * 100L))) // 2 minutes
                .signWith(getSecretKey()).compact();
    }

//    public boolean isValidMFAToken(final String token,
//                                   final String username) {
//        try {
//            final var tokenDigest = tokenDigest(token);
//            final var isRevokedToken = revokedJwtTokenRepository.isRevokedToken(tokenDigest);
//            return !isRevokedToken && Jwts
//                    .parser()
//                    .require("mfa", true)
//                    .requireSubject(username)
//                    .verifyWith(getSecretKey())
//                    .build()
//                    .isSigned(token);
//        } catch (final Exception e) {
//            return false;
//        }
//    }

    public String generateRefreshToken(final Map<String, Object> claims,
                                       final AppUser users) {
        return Jwts
                .builder()
                .claims(claims)
                .subject(users.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(getSecretKey())
                .compact();
    }

    public Claims extractAllClaims(final String token) {
        return Jwts
                .parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Claims extractAllClaims(final String token,
                                   final String tokenType) {
        return Jwts
                .parser()
                .require("typ", tokenType)
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractSpecificClaim(final String token,
                                      final Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public <T> T extractSpecificClaim(final String token,
                                      final String tokenType,
                                      final Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token, tokenType);
        return claimsResolver.apply(claims);
    }

    public String extractUsername(final String token) {
        return extractSpecificClaim(token, Claims::getSubject);
    }

    public String extractUsername(final String token,
                                  final String tokenType) {
        return extractSpecificClaim(token, tokenType, Claims::getSubject);
    }

    public boolean isTokenValid(final String token, final UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public boolean isTokenExpired(final String token) {
        final var tokenDigest = tokenDigest(token);
        //final var isRevokedToken = revokedJwtTokenRepository.isRevokedToken(tokenDigest);
        //return extractSpecificClaim(token, Claims::getExpiration).before(new Date()) || isRevokedToken;
        return extractSpecificClaim(token, Claims::getExpiration).before(new Date());
    }

    public String tokenDigest(final String token) {
        final var digestBytes = MESSAGE_DIGEST.digest(token.getBytes(StandardCharsets.UTF_8));
        return ENCODER.encodeToString(digestBytes);
    }

    public Claims extractAllClaimsWithNoCheckExpirationData(final String token) {
        try {
            return Jwts
                    .parser()
                    .verifyWith(getSecretKey())
                    .clockSkewSeconds(30 * 24 * 60) // skew clock 1 month
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (final Exception e) {
            return Jwts.claims().build();
        }
    }

    public String generateToken() {
        final var randomByte = new byte[24];
        SECURE_RANDOM.nextBytes(randomByte);
        return ENCODER.encodeToString(randomByte);
    }
}
