package com.hoops.participation.adapter.in.web;

import com.hoops.participation.adapter.in.web.dto.ParticipantDetailResponse;
import com.hoops.participation.adapter.in.web.dto.ParticipationResponse;
import com.hoops.participation.adapter.in.web.dto.RejectParticipationRequest;
import com.hoops.participation.application.port.in.ApproveParticipationCommand;
import com.hoops.participation.application.port.in.ApproveParticipationUseCase;
import com.hoops.participation.application.port.in.CancelParticipationCommand;
import com.hoops.participation.application.port.in.CancelParticipationUseCase;
import com.hoops.participation.application.port.in.GetMatchParticipantsUseCase;
import com.hoops.participation.application.port.in.ParticipateInMatchCommand;
import com.hoops.participation.application.port.in.ParticipateInMatchUseCase;
import com.hoops.participation.application.port.in.RejectParticipationCommand;
import com.hoops.participation.application.port.in.RejectParticipationUseCase;
import com.hoops.participation.application.port.out.UserInfo;
import com.hoops.participation.application.port.out.UserInfoProvider;
import com.hoops.participation.domain.Participation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Tag(name = "Participation", description = "경기 참가 관련 API")
@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class ParticipationController {

    private final ParticipateInMatchUseCase participateInMatchUseCase;
    private final CancelParticipationUseCase cancelParticipationUseCase;
    private final ApproveParticipationUseCase approveParticipationUseCase;
    private final RejectParticipationUseCase rejectParticipationUseCase;
    private final GetMatchParticipantsUseCase getMatchParticipantsUseCase;
    private final UserInfoProvider userInfoProvider;

    @Operation(summary = "경기 참가 신청", description = "경기에 참가를 신청합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "참가 신청 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (정원 초과, 중복 신청 등)"),
            @ApiResponse(responseCode = "401", description = "인증 필요"),
            @ApiResponse(responseCode = "404", description = "경기를 찾을 수 없음")
    })
    @PostMapping("/{matchId}/participations")
    public ResponseEntity<ParticipationResponse> participateInMatch(
            @Parameter(description = "경기 ID") @PathVariable("matchId") Long matchId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {

        ParticipateInMatchCommand command = new ParticipateInMatchCommand(matchId, userId);
        Participation participation = participateInMatchUseCase.participateInMatch(command);

        return ResponseEntity.created(
                URI.create("/api/matches/" + matchId + "/participations/" + participation.getId()))
                .body(ParticipationResponse.of(participation));
    }

    @Operation(summary = "경기 참가 취소", description = "경기 참가를 취소합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "참가 취소 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (본인이 아님)"),
            @ApiResponse(responseCode = "404", description = "참가 정보를 찾을 수 없음")
    })
    @DeleteMapping("/{matchId}/participations/{participationId}")
    public ResponseEntity<Void> cancelParticipation(
            @Parameter(description = "경기 ID") @PathVariable("matchId") Long matchId,
            @Parameter(description = "참가 ID") @PathVariable("participationId") Long participationId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {

        CancelParticipationCommand command = new CancelParticipationCommand(
                matchId, participationId, userId);
        cancelParticipationUseCase.cancelParticipation(command);

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "경기 참가자 목록 조회", description = "특정 경기의 참가자 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "경기를 찾을 수 없음")
    })
    @GetMapping("/{matchId}/participants")
    public ResponseEntity<List<ParticipantDetailResponse>> getMatchParticipants(
            @Parameter(description = "경기 ID") @PathVariable("matchId") Long matchId) {
        List<Participation> participants = getMatchParticipantsUseCase.getMatchParticipants(matchId);

        List<Long> userIds = participants.stream()
                .map(Participation::getUserId)
                .toList();
        Map<Long, UserInfo> userInfoMap = userInfoProvider.getUserInfoByIds(userIds);

        List<ParticipantDetailResponse> response = participants.stream()
                .map(p -> ParticipantDetailResponse.of(p, userInfoMap.get(p.getUserId())))
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "참가 신청 승인", description = "호스트가 참가 신청을 승인합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "승인 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (호스트가 아님)"),
            @ApiResponse(responseCode = "404", description = "참가 정보를 찾을 수 없음")
    })
    @PutMapping("/{matchId}/participations/{participationId}/approve")
    public ResponseEntity<ParticipationResponse> approveParticipation(
            @Parameter(description = "경기 ID") @PathVariable("matchId") Long matchId,
            @Parameter(description = "참가 ID") @PathVariable("participationId") Long participationId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {

        ApproveParticipationCommand command = new ApproveParticipationCommand(
                matchId, participationId, userId);
        Participation participation = approveParticipationUseCase.approveParticipation(command);

        return ResponseEntity.ok(ParticipationResponse.of(participation));
    }

    @Operation(summary = "참가 신청 거절", description = "호스트가 참가 신청을 거절합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "거절 성공"),
            @ApiResponse(responseCode = "400", description = "거절 사유 누락"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (호스트가 아님)"),
            @ApiResponse(responseCode = "404", description = "참가 정보를 찾을 수 없음")
    })
    @PutMapping("/{matchId}/participations/{participationId}/reject")
    public ResponseEntity<ParticipationResponse> rejectParticipation(
            @Parameter(description = "경기 ID") @PathVariable("matchId") Long matchId,
            @Parameter(description = "참가 ID") @PathVariable("participationId") Long participationId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody RejectParticipationRequest request) {

        RejectParticipationCommand command = new RejectParticipationCommand(
                matchId, participationId, userId, request.reason());
        Participation participation = rejectParticipationUseCase.rejectParticipation(command);

        return ResponseEntity.ok(ParticipationResponse.of(participation));
    }
}
