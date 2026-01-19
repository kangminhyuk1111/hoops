package com.hoops.acceptance.mock;

import com.hoops.auth.application.dto.KakaoTokenResponse;
import com.hoops.auth.application.dto.KakaoUserInfo;
import com.hoops.auth.domain.port.KakaoOAuthClient;
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
public class MockKakaoOAuthClient implements KakaoOAuthClient {

    private static final Logger log = LoggerFactory.getLogger(MockKakaoOAuthClient.class);

    private final Map<String, KakaoUserInfo> codeToUserInfo = new ConcurrentHashMap<>();

    @Override
    public String getAuthorizationUrl() {
        return "https://kauth.kakao.com/oauth/authorize?mock=true";
    }

    @Override
    public KakaoTokenResponse getToken(String code) {
        log.info("[MOCK] 카카오 토큰 교환 요청: code={}", code);
        return new KakaoTokenResponse("mock-kakao-access-token-" + code, "mock-kakao-refresh-token", 3600);
    }

    @Override
    public KakaoUserInfo getUserInfo(String accessToken) {
        log.info("[MOCK] 카카오 사용자 정보 조회: accessToken={}", accessToken);

        String code = accessToken.replace("mock-kakao-access-token-", "");
        KakaoUserInfo userInfo = codeToUserInfo.get(code);

        if (userInfo != null) {
            log.info("[MOCK] 등록된 사용자 정보 반환: kakaoId={}", userInfo.kakaoId());
            return userInfo;
        }

        KakaoUserInfo defaultUser = new KakaoUserInfo(
                "default-kakao-id-" + code,
                "test" + code + "@kakao.com",
                "테스트유저",
                "https://example.com/profile.jpg"
        );
        log.info("[MOCK] 기본 사용자 정보 반환: kakaoId={}", defaultUser.kakaoId());
        return defaultUser;
    }

    /**
     * 테스트용으로 인증 코드에 대한 카카오 사용자 정보를 등록합니다.
     *
     * @param code 카카오 인가 코드
     * @param userInfo 사용자 정보
     */
    public void registerUser(String code, KakaoUserInfo userInfo) {
        codeToUserInfo.put(code, userInfo);
        log.info("[MOCK] 사용자 등록: code={}, kakaoId={}", code, userInfo.kakaoId());
    }

    /**
     * 등록된 사용자 정보를 모두 삭제합니다.
     */
    public void clear() {
        codeToUserInfo.clear();
        log.info("[MOCK] 등록된 사용자 정보 초기화");
    }
}
