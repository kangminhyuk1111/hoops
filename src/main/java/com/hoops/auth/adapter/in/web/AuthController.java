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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Auth", description = "인증 관련 API")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final KakaoLoginUseCase kakaoLoginUseCase;
    private final SignupUseCase signupUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;

    @Operation(summary = "카카오 인증 URL 조회", description = "카카오 OAuth 로그인을 위한 인증 URL을 반환합니다.")
    @ApiResponse(responseCode = "200", description = "인증 URL 반환 성공")
    @GetMapping("/kakao")
    public ResponseEntity<KakaoAuthUrlResponse> getKakaoAuthUrl() {
        log.info("[카카오 인증] 인증 URL 요청");
        String authUrl = kakaoLoginUseCase.getKakaoAuthUrl();
        log.info("[카카오 인증] 생성된 URL: {}", authUrl);
        return ResponseEntity.ok(new KakaoAuthUrlResponse(authUrl));
    }

    @Operation(summary = "카카오 콜백 처리", description = "카카오 인증 후 콜백을 처리합니다. 기존 회원은 토큰을 반환하고, 신규 회원은 임시 토큰을 반환합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "기존 회원 로그인 성공"),
            @ApiResponse(responseCode = "202", description = "신규 회원 - 회원가입 필요")
    })
    @GetMapping("/kakao/callback")
    public ResponseEntity<KakaoCallbackResponse> handleKakaoCallback(
            @Parameter(description = "카카오 인증 코드") @RequestParam("code") String code) {
        KakaoCallbackResult result = kakaoLoginUseCase.processCallback(code);
        KakaoCallbackResponse response = KakaoCallbackResponse.from(result);

        if (result.isNewUser()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "회원가입", description = "신규 회원의 회원가입을 완료합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "회원가입 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(
            @Valid @RequestBody SignupRequest request) {
        AuthResult result = signupUseCase.signup(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(result));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "토큰 갱신 성공"),
            @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        TokenResult result = refreshTokenUseCase.refresh(request.refreshToken());
        return ResponseEntity.ok(TokenResponse.from(result));
    }
}
