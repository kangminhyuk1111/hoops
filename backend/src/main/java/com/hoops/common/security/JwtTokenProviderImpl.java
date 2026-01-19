package com.hoops.common.security;

import com.hoops.auth.application.port.out.JwtTokenPort;
import com.hoops.auth.application.exception.InvalidRefreshTokenException;
import com.hoops.auth.application.exception.InvalidTempTokenException;
import com.hoops.auth.domain.vo.TokenPair;
import com.hoops.common.exception.InvalidTokenClaimException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Component;

/**
 * JWT Token Provider Implementation.
 */
@Component
public class JwtTokenProviderImpl implements JwtTokenPort {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String TOKEN_TYPE_ACCESS = "access";
    private static final String TOKEN_TYPE_REFRESH = "refresh";
    private static final String TOKEN_TYPE_TEMP = "temp";
    private static final String USER_ID_CLAIM = "userId";

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtTokenProviderImpl(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.secret().getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public TokenPair createTokens(Long userId) {
        String accessToken = createAccessToken(userId);
        String refreshToken = createRefreshToken(userId);
        return TokenPair.of(accessToken, refreshToken);
    }

    @Override
    public String createTempToken(Map<String, Object> claims) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.tempTokenExpiry());

        Map<String, Object> allClaims = new HashMap<>(claims);
        allClaims.put(TOKEN_TYPE_CLAIM, TOKEN_TYPE_TEMP);

        return Jwts.builder()
                .claims(allClaims)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    @Override
    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaimsFromToken(token);
        Object userId = claims.get(USER_ID_CLAIM);

        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        }

        throw new InvalidTokenClaimException("Valid userId not found in token");
    }

    @Override
    public Map<String, Object> getClaimsFromTempToken(String tempToken) {
        try {
            Claims claims = parseClaimsFromToken(tempToken);

            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            if (!TOKEN_TYPE_TEMP.equals(tokenType)) {
                throw new InvalidTempTokenException("Not a temporary token");
            }

            Map<String, Object> result = new HashMap<>();
            claims.forEach(result::put);
            result.remove(TOKEN_TYPE_CLAIM);

            return result;
        } catch (ExpiredJwtException e) {
            throw new InvalidTempTokenException("Temporary token has expired");
        } catch (JwtException e) {
            throw new InvalidTempTokenException();
        }
    }

    @Override
    public TokenPair refreshTokens(String refreshToken) {
        try {
            Claims claims = parseClaimsFromToken(refreshToken);

            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            if (!TOKEN_TYPE_REFRESH.equals(tokenType)) {
                throw new InvalidRefreshTokenException("Not a refresh token");
            }

            Long userId = getUserIdFromClaims(claims);
            return createTokens(userId);
        } catch (ExpiredJwtException e) {
            throw new InvalidRefreshTokenException("Refresh token has expired");
        } catch (JwtException e) {
            throw new InvalidRefreshTokenException();
        }
    }

    @Override
    public boolean validateToken(String token) {
        try {
            parseClaimsFromToken(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private String createAccessToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.accessTokenExpiry());

        return Jwts.builder()
                .claim(USER_ID_CLAIM, userId)
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_ACCESS)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    private String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.refreshTokenExpiry());

        return Jwts.builder()
                .claim(USER_ID_CLAIM, userId)
                .claim(TOKEN_TYPE_CLAIM, TOKEN_TYPE_REFRESH)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    private Claims parseClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private Long getUserIdFromClaims(Claims claims) {
        Object userId = claims.get(USER_ID_CLAIM);

        if (userId instanceof Integer) {
            return ((Integer) userId).longValue();
        } else if (userId instanceof Long) {
            return (Long) userId;
        }

        throw new InvalidTokenClaimException("Valid userId not found in claims");
    }
}
