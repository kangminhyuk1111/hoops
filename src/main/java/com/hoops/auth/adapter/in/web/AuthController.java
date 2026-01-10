package com.hoops.auth.adapter.in.web;

import com.hoops.auth.adapter.in.web.dto.AuthResponse;
import com.hoops.auth.adapter.in.web.dto.KakaoAuthUrlResponse;
import com.hoops.auth.adapter.in.web.dto.KakaoCallbackResponse;
import com.hoops.auth.adapter.in.web.dto.RefreshTokenRequest;
import com.hoops.auth.adapter.in.web.dto.SignupRequest;
import com.hoops.auth.adapter.in.web.dto.TokenResponse;
import com.hoops.auth.application.dto.AuthResult;
import com.hoops.auth.application.dto.KakaoCallbackResult;
import com.hoops.auth.application.dto.TokenResult;
import com.hoops.auth.application.port.in.KakaoLoginUseCase;
import com.hoops.auth.application.port.in.RefreshTokenUseCase;
import com.hoops.auth.application.port.in.SignupUseCase;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 컨트롤러
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final KakaoLoginUseCase kakaoLoginUseCase;
    private final SignupUseCase signupUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    public AuthController(
            KakaoLoginUseCase kakaoLoginUseCase,
            SignupUseCase signupUseCase,
            RefreshTokenUseCase refreshTokenUseCase) {
        this.kakaoLoginUseCase = kakaoLoginUseCase;
        this.signupUseCase = signupUseCase;
        this.refreshTokenUseCase = refreshTokenUseCase;
    }

    /**
     * 카카오 인증 URL 반환
     *
     * @return 카카오 인증 페이지 URL
     */
    @GetMapping("/kakao")
    public ResponseEntity<KakaoAuthUrlResponse> getKakaoAuthUrl() {
        log.info("[카카오 인증] 인증 URL 요청");
        String authUrl = kakaoLoginUseCase.getKakaoAuthUrl();
        log.info("[카카오 인증] 생성된 URL: {}", authUrl);
        return ResponseEntity.ok(new KakaoAuthUrlResponse(authUrl));
    }

    /**
     * 카카오 콜백 처리
     *
     * @param code 카카오 인가코드
     * @return 신규 회원: 202 + 임시토큰, 기존 회원: 200 + JWT
     */
    @GetMapping("/kakao/callback")
    public ResponseEntity<KakaoCallbackResponse> handleKakaoCallback(
            @RequestParam("code") String code) {
        KakaoCallbackResult result = kakaoLoginUseCase.processCallback(code);
        KakaoCallbackResponse response = KakaoCallbackResponse.from(result);

        if (result.isNewUser()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 회원가입 완료
     *
     * @param request 임시 토큰 + 닉네임
     * @return JWT 토큰 + 사용자 정보
     */
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody SignupRequest request) {
        AuthResult result = signupUseCase.signup(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(result));
    }

    /**
     * 토큰 갱신
     *
     * @param request 리프레시 토큰
     * @return 새로운 토큰
     */
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        TokenResult result = refreshTokenUseCase.refresh(request.refreshToken());
        return ResponseEntity.ok(TokenResponse.from(result));
    }
}
