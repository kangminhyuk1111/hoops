package com.hoops.auth.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Kakao OAuth configuration properties.
 */
@ConfigurationProperties(prefix = "kakao")
public class KakaoOAuthProperties {

    private static final Logger log = LoggerFactory.getLogger(KakaoOAuthProperties.class);
    private static final String DEFAULT_AUTH_BASE_URL = "https://kauth.kakao.com";
    private static final String DEFAULT_API_BASE_URL = "https://kapi.kakao.com";

    @PostConstruct
    public void logConfig() {
        log.info("=== Kakao OAuth Configuration ===");
        log.info("Client ID: {}", clientId != null ? clientId.substring(0, Math.min(8, clientId.length())) + "..." : "NULL");
        log.info("Redirect URI: {}", redirectUri);
        log.info("Auth Base URL: {}", authBaseUrl);
        log.info("=================================");
    }

    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String authBaseUrl = DEFAULT_AUTH_BASE_URL;
    private String apiBaseUrl = DEFAULT_API_BASE_URL;

    public KakaoOAuthProperties() {
    }

    public KakaoOAuthProperties(String clientId, String clientSecret, String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getAuthBaseUrl() {
        return authBaseUrl;
    }

    public void setAuthBaseUrl(String authBaseUrl) {
        this.authBaseUrl = authBaseUrl;
    }

    public String getApiBaseUrl() {
        return apiBaseUrl;
    }

    public void setApiBaseUrl(String apiBaseUrl) {
        this.apiBaseUrl = apiBaseUrl;
    }
}
