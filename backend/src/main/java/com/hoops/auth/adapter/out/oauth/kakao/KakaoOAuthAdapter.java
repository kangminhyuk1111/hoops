package com.hoops.auth.adapter.out.oauth.kakao;

import com.hoops.auth.adapter.out.oauth.kakao.exception.InvalidAuthCodeException;
import com.hoops.auth.adapter.out.oauth.kakao.exception.KakaoApiException;
import com.hoops.auth.application.port.out.OAuthPort;
import com.hoops.auth.domain.vo.OAuthTokenInfo;
import com.hoops.auth.domain.vo.OAuthUserInfo;
import com.hoops.auth.infrastructure.config.KakaoOAuthProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class KakaoOAuthAdapter implements OAuthPort {

    private static final ParameterizedTypeReference<Map<String, Object>> MAP_TYPE = new ParameterizedTypeReference<>() {};

    private final KakaoOAuthProperties properties;
    private final RestTemplate restTemplate;

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
            Map<String, Object> response = requestToken(code);
            return toOAuthTokenInfo(response);
        } catch (HttpClientErrorException e) {
            throw new InvalidAuthCodeException("Authorization code is expired or invalid");
        } catch (RestClientException e) {
            throw new KakaoApiException("Failed to exchange Kakao token", e);
        }
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        try {
            Map<String, Object> response = requestUserInfo(accessToken);
            return toOAuthUserInfo(response);
        } catch (RestClientException e) {
            throw new KakaoApiException("Failed to fetch Kakao user info", e);
        }
    }

    private Map<String, Object> requestToken(String code) {
        HttpHeaders headers = createFormHeaders();
        MultiValueMap<String, String> params = createTokenParams(code);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                properties.getAuthBaseUrl() + "/oauth/token",
                HttpMethod.POST,
                new HttpEntity<>(params, headers),
                MAP_TYPE);

        return extractBody(response, "Kakao token response is empty");
    }

    private Map<String, Object> requestUserInfo(String accessToken) {
        HttpHeaders headers = createBearerHeaders(accessToken);

        ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                properties.getApiBaseUrl() + "/v2/user/me",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                MAP_TYPE);

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

    private Map<String, Object> extractBody(ResponseEntity<Map<String, Object>> response, String errorMessage) {
        return Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new KakaoApiException(errorMessage));
    }

    private OAuthTokenInfo toOAuthTokenInfo(Map<String, Object> body) {
        return OAuthTokenInfo.of(
                (String) body.get("access_token"),
                (String) body.get("refresh_token"),
                (Integer) body.get("expires_in"));
    }

    @SuppressWarnings("unchecked")
    private OAuthUserInfo toOAuthUserInfo(Map<String, Object> body) {
        String kakaoId = String.valueOf(body.get("id"));
        Map<String, Object> account = getNestedMap(body, "kakao_account");
        Map<String, Object> profile = getNestedMap(account, "profile");

        return OAuthUserInfo.of(
                kakaoId,
                (String) account.get("email"),
                (String) profile.get("nickname"),
                (String) profile.get("profile_image_url"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getNestedMap(Map<String, Object> map, String key) {
        return Optional.ofNullable(map)
                .map(m -> (Map<String, Object>) m.get(key))
                .orElse(Collections.emptyMap());
    }
}
