package com.hoops.auth.adapter.in.web.dto;

import com.hoops.auth.application.dto.KakaoCallbackResult;
import com.hoops.auth.domain.vo.KakaoUserInfo;

public record KakaoCallbackResponse(
        boolean isNewUser,
        String tempToken,
        String accessToken,
        String refreshToken,
        KakaoInfoResponse kakaoInfo,
        UserResponse user
) {
    public static KakaoCallbackResponse from(KakaoCallbackResult result) {
        KakaoInfoResponse kakaoInfo = null;
        if (result.kakaoInfo() != null) {
            kakaoInfo = KakaoInfoResponse.from(result.kakaoInfo());
        }

        UserResponse user = null;
        if (result.user() != null) {
            user = UserResponse.from(result.user());
        }

        return new KakaoCallbackResponse(
                result.isNewUser(),
                result.tempToken(),
                result.accessToken(),
                result.refreshToken(),
                kakaoInfo,
                user
        );
    }

    public record KakaoInfoResponse(
            String email,
            String profileImage
    ) {
        public static KakaoInfoResponse from(KakaoUserInfo info) {
            return new KakaoInfoResponse(info.email(), info.profileImage());
        }
    }
}
