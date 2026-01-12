package com.hoops.user.adapter.in.web;

import com.hoops.participation.adapter.in.web.dto.ParticipationResponse;
import com.hoops.participation.application.port.in.GetMyParticipationsUseCase;
import com.hoops.participation.domain.Participation;
import com.hoops.user.adapter.in.web.dto.UpdateUserRequest;
import com.hoops.user.adapter.in.web.dto.UserResponse;
import com.hoops.user.application.port.in.GetMyProfileUseCase;
import com.hoops.user.application.port.in.GetUserProfileUseCase;
import com.hoops.user.application.port.in.UpdateUserProfileUseCase;
import com.hoops.user.domain.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "User", description = "사용자 관련 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetMyProfileUseCase getMyProfileUseCase;
    private final GetUserProfileUseCase getUserProfileUseCase;
    private final UpdateUserProfileUseCase updateUserProfileUseCase;
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

    @Operation(summary = "사용자 프로필 조회", description = "특정 사용자의 프로필 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음")
    })
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {
        User user = getUserProfileUseCase.getUserProfile(userId);
        return ResponseEntity.ok(UserResponse.of(user));
    }

    @Operation(summary = "프로필 수정", description = "사용자 프로필을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "사용자를 찾을 수 없음"),
            @ApiResponse(responseCode = "409", description = "중복된 닉네임")
    })
    @PutMapping("/{userId}")
    public ResponseEntity<UserResponse> updateUserProfile(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long requesterId,
            @Valid @RequestBody UpdateUserRequest request) {
        User user = updateUserProfileUseCase.updateUserProfile(request.toCommand(userId, requesterId));
        return ResponseEntity.ok(UserResponse.of(user));
    }
}
