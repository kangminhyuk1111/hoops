package com.hoops.common.security;

import com.hoops.auth.application.dto.TokenResult;
import com.hoops.auth.application.port.out.JwtTokenProvider;
import com.hoops.user.application.exception.InvalidRefreshTokenException;
import com.hoops.user.application.exception.InvalidTempTokenException;
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
 * JWT 토큰 제공자 구현체
 */
@Component
public class JwtTokenProviderImpl implements JwtTokenProvider {

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
    public TokenResult createTokens(Long userId) {
        String accessToken = createAccessToken(userId);
        String refreshToken = createRefreshToken(userId);
        return new TokenResult(accessToken, refreshToken);
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

        throw new IllegalArgumentException("Invalid userId in token");
    }

    @Override
    public Map<String, Object> getClaimsFromTempToken(String tempToken) {
        try {
            Claims claims = parseClaimsFromToken(tempToken);

            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            if (!TOKEN_TYPE_TEMP.equals(tokenType)) {
                throw new InvalidTempTokenException("임시 토큰이 아닙니다");
            }

            Map<String, Object> result = new HashMap<>();
            claims.forEach(result::put);
            result.remove(TOKEN_TYPE_CLAIM);

            return result;
        } catch (ExpiredJwtException e) {
            throw new InvalidTempTokenException("임시 토큰이 만료되었습니다");
        } catch (JwtException e) {
            throw new InvalidTempTokenException();
        }
    }

    @Override
    public TokenResult refreshTokens(String refreshToken) {
        try {
            Claims claims = parseClaimsFromToken(refreshToken);

            String tokenType = claims.get(TOKEN_TYPE_CLAIM, String.class);
            if (!TOKEN_TYPE_REFRESH.equals(tokenType)) {
                throw new InvalidRefreshTokenException("리프레시 토큰이 아닙니다");
            }

            Long userId = getUserIdFromClaims(claims);
            return createTokens(userId);
        } catch (ExpiredJwtException e) {
            throw new InvalidRefreshTokenException("리프레시 토큰이 만료되었습니다");
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

        throw new IllegalArgumentException("Invalid userId in claims");
    }
}
