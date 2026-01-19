package com.hoops.auth.adapter.out.oauth;

import com.hoops.auth.application.dto.KakaoTokenResponse;
import com.hoops.auth.application.dto.KakaoUserInfo;
import com.hoops.auth.domain.port.KakaoOAuthClient;
import com.hoops.auth.infrastructure.config.KakaoOAuthProperties;
import com.hoops.user.application.exception.InvalidAuthCodeException;
import com.hoops.user.application.exception.KakaoApiException;
import java.util.Map;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Kakao OAuth client adapter implementation.
 */
@Component
public class KakaoOAuthClientImpl implements KakaoOAuthClient {

    private static final String AUTHORIZE_PATH = "/oauth/authorize";
    private static final String TOKEN_PATH = "/oauth/token";
    private static final String USER_INFO_PATH = "/v2/user/me";

    private final KakaoOAuthProperties properties;
    private final RestTemplate restTemplate;

    public KakaoOAuthClientImpl(KakaoOAuthProperties properties, RestTemplate restTemplate) {
        this.properties = properties;
        this.restTemplate = restTemplate;
    }

    @Override
    public String getAuthorizationUrl() {
        return properties.getAuthBaseUrl() + AUTHORIZE_PATH
                + "?client_id=" + properties.getClientId()
                + "&redirect_uri=" + properties.getRedirectUri()
                + "&response_type=code";
    }

    @Override
    public KakaoTokenResponse getToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", properties.getClientId());
        params.add("redirect_uri", properties.getRedirectUri());
        params.add("code", code);

        if (properties.getClientSecret() != null && !properties.getClientSecret().isBlank()) {
            params.add("client_secret", properties.getClientSecret());
        }

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    properties.getAuthBaseUrl() + TOKEN_PATH,
                    HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new KakaoApiException("Kakao token response is empty");
            }

            return new KakaoTokenResponse(
                    (String) body.get("access_token"),
                    (String) body.get("refresh_token"),
                    (Integer) body.get("expires_in")
            );
        } catch (HttpClientErrorException e) {
            throw new InvalidAuthCodeException("Authorization code is expired or invalid");
        } catch (RestClientException e) {
            throw new KakaoApiException("Failed to exchange Kakao token", e);
        }
    }

    @Override
    public KakaoUserInfo getUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    properties.getApiBaseUrl() + USER_INFO_PATH,
                    HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body == null) {
                throw new KakaoApiException("Kakao user info response is empty");
            }

            return parseKakaoUserInfo(body);
        } catch (RestClientException e) {
            throw new KakaoApiException("Failed to fetch Kakao user info", e);
        }
    }

    @SuppressWarnings("unchecked")
    private KakaoUserInfo parseKakaoUserInfo(Map<String, Object> body) {
        String kakaoId = String.valueOf(body.get("id"));

        Map<String, Object> kakaoAccount = (Map<String, Object>) body.get("kakao_account");
        String email = null;
        String nickname = null;
        String profileImage = null;

        if (kakaoAccount != null) {
            email = (String) kakaoAccount.get("email");

            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            if (profile != null) {
                nickname = (String) profile.get("nickname");
                profileImage = (String) profile.get("profile_image_url");
            }
        }

        return new KakaoUserInfo(kakaoId, email, nickname, profileImage);
    }
}
