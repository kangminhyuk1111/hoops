package com.hoops.auth.adapter.out.oauth.kakao;

import com.hoops.auth.adapter.out.oauth.kakao.dto.KakaoTokenResponse;
import com.hoops.auth.adapter.out.oauth.kakao.dto.KakaoUserResponse;
import com.hoops.auth.adapter.out.oauth.kakao.exception.InvalidAuthCodeException;
import com.hoops.auth.adapter.out.oauth.kakao.exception.KakaoApiException;
import com.hoops.auth.application.port.out.OAuthPort;
import com.hoops.auth.domain.vo.AuthProvider;
import com.hoops.auth.domain.vo.OAuthTokenInfo;
import com.hoops.auth.domain.vo.OAuthUserInfo;
import com.hoops.auth.infrastructure.config.KakaoOAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class KakaoOAuthAdapter implements OAuthPort {

    private final KakaoOAuthProperties properties;
    private final RestTemplate restTemplate;

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.KAKAO;
    }

    @Override
    public String getAuthorizationUrl() {
        return String.format("%s/oauth/authorize?client_id=%s&redirect_uri=%s&response_type=code",
                properties.getAuthBaseUrl(),
                properties.getClientId(),
                properties.getRedirectUri());
    }

    @Override
    public OAuthTokenInfo getToken(String code) {
        try {
            KakaoTokenResponse response = requestToken(code);
            return response.toOAuthTokenInfo();
        } catch (HttpClientErrorException e) {
            throw new InvalidAuthCodeException("Authorization code is expired or invalid");
        } catch (RestClientException e) {
            throw new KakaoApiException("Failed to exchange Kakao token", e);
        }
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        try {
            KakaoUserResponse response = requestUserInfo(accessToken);
            return response.toOAuthUserInfo();
        } catch (RestClientException e) {
            throw new KakaoApiException("Failed to fetch Kakao user info", e);
        }
    }

    private KakaoTokenResponse requestToken(String code) {
        HttpHeaders headers = createFormHeaders();
        MultiValueMap<String, String> params = createTokenParams(code);

        ResponseEntity<KakaoTokenResponse> response = restTemplate.exchange(
                properties.getAuthBaseUrl() + "/oauth/token",
                HttpMethod.POST,
                new HttpEntity<>(params, headers),
                KakaoTokenResponse.class);

        return extractBody(response, "Kakao token response is empty");
    }

    private KakaoUserResponse requestUserInfo(String accessToken) {
        HttpHeaders headers = createBearerHeaders(accessToken);

        ResponseEntity<KakaoUserResponse> response = restTemplate.exchange(
                properties.getApiBaseUrl() + "/v2/user/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                KakaoUserResponse.class);

        return extractBody(response, "Kakao user info response is empty");
    }

    private HttpHeaders createFormHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private HttpHeaders createBearerHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private MultiValueMap<String, String> createTokenParams(String code) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", properties.getClientId());
        params.add("redirect_uri", properties.getRedirectUri());
        params.add("code", code);

        Optional.ofNullable(properties.getClientSecret())
                .filter(secret -> !secret.isBlank())
                .ifPresent(secret -> params.add("client_secret", secret));

        return params;
    }

    private <T> T extractBody(ResponseEntity<T> response, String errorMessage) {
        return Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new KakaoApiException(errorMessage));
    }
}
