package com.hoops.user.adapter.in.web;

import com.hoops.participation.adapter.in.web.dto.ParticipationResponse;
import com.hoops.participation.application.port.in.GetMyParticipationsUseCase;
import com.hoops.participation.domain.Participation;
import com.hoops.user.adapter.in.web.dto.UserResponse;
import com.hoops.user.application.port.in.GetMyProfileUseCase;
import com.hoops.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetMyProfileUseCase getMyProfileUseCase;
    private final GetMyParticipationsUseCase getMyParticipationsUseCase;

    @Operation(summary = "내 프로필 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        User user = getMyProfileUseCase.getMyProfile(userId);
        return ResponseEntity.ok(UserResponse.of(user));
    }

    @Operation(summary = "내 참가 경기 목록 조회", description = "로그인한 사용자가 참가한 경기 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/me/participations")
    public ResponseEntity<List<ParticipationResponse>> getMyParticipations(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        List<Participation> participations = getMyParticipationsUseCase.getMyParticipations(userId);
        List<ParticipationResponse> response = participations.stream()
                .map(ParticipationResponse::of)
                .toList();
        return ResponseEntity.ok(response);
    }
}
