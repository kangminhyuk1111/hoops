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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoLoginUseCase kakaoLoginUseCase;
    private final SignupUseCase signupUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @GetMapping("/kakao")
    public ResponseEntity<KakaoAuthUrlResponse> getKakaoAuthUrl() {
        log.info("[카카오 인증] 인증 URL 요청");
        String authUrl = kakaoLoginUseCase.getKakaoAuthUrl();
        log.info("[카카오 인증] 생성된 URL: {}", authUrl);
        return ResponseEntity.ok(new KakaoAuthUrlResponse(authUrl));
    }

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

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody SignupRequest request) {
        AuthResult result = signupUseCase.signup(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(result));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        TokenResult result = refreshTokenUseCase.refresh(request.refreshToken());
        return ResponseEntity.ok(TokenResponse.from(result));
    }
}
