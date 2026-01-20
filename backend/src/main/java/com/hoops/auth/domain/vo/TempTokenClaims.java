package com.hoops.auth.domain.vo;

/**
 * Temporary token claims for pre-signup OAuth flow.
 * Contains user information from OAuth provider before account creation.
 */
public record TempTokenClaims(
        AuthProvider provider,
        String providerId,
        String email,
        String profileImage
) {
    public static TempTokenClaims of(AuthProvider provider, String providerId, String email, String profileImage) {
        return new TempTokenClaims(provider, providerId, email, profileImage);
    }

    public static TempTokenClaims fromOAuthUserInfo(AuthProvider provider, OAuthUserInfo userInfo) {
        return new TempTokenClaims(
                provider,
                userInfo.providerId(),
                userInfo.email(),
                userInfo.profileImage()
        );
    }
}
