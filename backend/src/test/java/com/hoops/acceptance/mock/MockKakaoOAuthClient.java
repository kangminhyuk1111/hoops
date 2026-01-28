package com.hoops.acceptance.mock;

import com.hoops.auth.application.port.out.OAuthPort;
import com.hoops.auth.domain.vo.AuthProvider;
import com.hoops.auth.domain.vo.OAuthTokenInfo;
import com.hoops.auth.domain.vo.OAuthUserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 테스트용 Mock 카카오 OAuth 클라이언트
 * 외부 카카오 API 대신 메모리에서 인증 처리를 시뮬레이션합니다.
 */
@Component
@Profile("test")
@Primary
public class MockKakaoOAuthClient implements OAuthPort {

    private static final Logger log = LoggerFactory.getLogger(MockKakaoOAuthClient.class);

    private final Map<String, OAuthUserInfo> codeToUserInfo = new ConcurrentHashMap<>();

    @Override
    public AuthProvider getProvider() {
        return AuthProvider.KAKAO;
    }

    @Override
    public String getAuthorizationUrl() {
        return "https://kauth.kakao.com/oauth/authorize?mock=true";
    }

    @Override
    public OAuthTokenInfo getToken(String code) {
        log.info("[MOCK] 카카오 토큰 교환 요청: code={}", code);
        return OAuthTokenInfo.of("mock-kakao-access-token-" + code, "mock-kakao-refresh-token", 3600);
    }

    @Override
    public OAuthUserInfo getUserInfo(String accessToken) {
        log.info("[MOCK] 카카오 사용자 정보 조회: accessToken={}", accessToken);

        String code = accessToken.replace("mock-kakao-access-token-", "");
        OAuthUserInfo userInfo = codeToUserInfo.get(code);

        if (userInfo != null) {
            log.info("[MOCK] 등록된 사용자 정보 반환: providerId={}", userInfo.providerId());
            return userInfo;
        }

        OAuthUserInfo defaultUser = OAuthUserInfo.of(
                "default-kakao-id-" + code,
                "test" + code + "@kakao.com",
                "테스트유저",
                "https://example.com/profile.jpg"
        );
        log.info("[MOCK] 기본 사용자 정보 반환: providerId={}", defaultUser.providerId());
        return defaultUser;
    }

    /**
     * 테스트용으로 인증 코드에 대한 OAuth 사용자 정보를 등록합니다.
     *
     * @param code 인가 코드
     * @param userInfo 사용자 정보
     */
    public void registerUser(String code, OAuthUserInfo userInfo) {
        codeToUserInfo.put(code, userInfo);
        log.info("[MOCK] 사용자 등록: code={}, providerId={}", code, userInfo.providerId());
    }

    /**
     * 등록된 사용자 정보를 모두 삭제합니다.
     */
    public void clear() {
        codeToUserInfo.clear();
        log.info("[MOCK] 등록된 사용자 정보 초기화");
    }
}
