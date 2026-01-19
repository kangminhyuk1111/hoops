package com.hoops.auth.adapter.in.web;

import com.hoops.auth.adapter.in.web.dto.AuthResponse;
import com.hoops.auth.adapter.in.web.dto.AuthUrlResponse;
import com.hoops.auth.adapter.in.web.dto.OAuthCallbackResponse;
import com.hoops.auth.adapter.in.web.dto.RefreshTokenRequest;
import com.hoops.auth.adapter.in.web.dto.SignupRequest;
import com.hoops.auth.adapter.in.web.dto.TokenResponse;
import com.hoops.auth.application.dto.AuthResult;
import com.hoops.auth.application.dto.OAuthCallbackResult;
import com.hoops.auth.application.port.in.OAuthLoginUseCase;
import com.hoops.auth.application.port.in.SignupUseCase;
import com.hoops.auth.application.port.in.TokenUseCase;
import com.hoops.auth.domain.vo.AuthProvider;
import com.hoops.auth.domain.vo.TokenPair;
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

@Tag(name = "Auth", description = "Authentication APIs")
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OAuthLoginUseCase oauthLoginUseCase;
    private final SignupUseCase signupUseCase;
    private final TokenUseCase tokenUseCase;

    @Operation(summary = "Get OAuth authorization URL")
    @ApiResponse(responseCode = "200", description = "Auth URL returned successfully")
    @GetMapping("/kakao")
    public ResponseEntity<AuthUrlResponse> getKakaoAuthUrl() {
        log.info("OAuth auth URL requested for Kakao");
        String authUrl = oauthLoginUseCase.getAuthorizationUrl(AuthProvider.KAKAO);
        return ResponseEntity.ok(new AuthUrlResponse(authUrl));
    }

    @Operation(summary = "Handle OAuth callback")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Existing user login successful"),
            @ApiResponse(responseCode = "202", description = "New user - signup required")
    })
    @GetMapping("/kakao/callback")
    public ResponseEntity<OAuthCallbackResponse> handleKakaoCallback(
            @Parameter(description = "OAuth authorization code") @RequestParam("code") String code) {
        OAuthCallbackResult result = oauthLoginUseCase.processCallback(AuthProvider.KAKAO, code);
        OAuthCallbackResponse response = OAuthCallbackResponse.from(result);

        if (result.isNewUser()) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Sign up")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Signup successful"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@Valid @RequestBody SignupRequest request) {
        AuthResult result = signupUseCase.signup(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.from(result));
    }

    @Operation(summary = "Refresh token")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refresh successful"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenPair tokenPair = tokenUseCase.refresh(request.refreshToken());
        return ResponseEntity.ok(TokenResponse.from(tokenPair));
    }
}
