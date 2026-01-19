package com.hoops.auth.domain.port;

import com.hoops.auth.application.dto.TokenResult;
import java.util.Map;

/**
 * JWT Token Provider Interface.
 *
 * Outbound port responsible for JWT token creation and validation.
 */
public interface JwtTokenProvider {

    /**
     * Creates access token and refresh token.
     *
     * @param userId user ID
     * @return token result
     */
    TokenResult createTokens(Long userId);

    /**
     * Creates a temporary token (used before signup completion).
     *
     * @param claims claims to include in the token
     * @return temporary token
     */
    String createTempToken(Map<String, Object> claims);

    /**
     * Extracts user ID from token.
     *
     * @param token JWT token
     * @return user ID
     */
    Long getUserIdFromToken(String token);

    /**
     * Extracts claims from temporary token.
     *
     * @param tempToken temporary token
     * @return claims map
     */
    Map<String, Object> getClaimsFromTempToken(String tempToken);

    /**
     * Issues new tokens using refresh token.
     *
     * @param refreshToken refresh token
     * @return new token result
     */
    TokenResult refreshTokens(String refreshToken);

    /**
     * Validates token.
     *
     * @param token JWT token
     * @return validity
     */
    boolean validateToken(String token);
}
