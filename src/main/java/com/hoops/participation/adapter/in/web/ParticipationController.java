package com.hoops.participation.adapter.in.web;

import com.hoops.participation.adapter.in.web.dto.ParticipationResponse;
import com.hoops.participation.application.port.in.CancelParticipationCommand;
import com.hoops.participation.application.port.in.CancelParticipationUseCase;
import com.hoops.participation.application.port.in.ParticipateInMatchCommand;
import com.hoops.participation.application.port.in.ParticipateInMatchUseCase;
import com.hoops.participation.domain.Participation;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 경기 참가 관련 REST API Controller
 *
 * Hexagonal Architecture의 Inbound Adapter로서,
 * HTTP 요청을 받아 UseCase를 호출하고 응답을 반환합니다.
 */
@RestController
@RequestMapping("/api/matches")
public class ParticipationController {

    private final ParticipateInMatchUseCase participateInMatchUseCase;
    private final CancelParticipationUseCase cancelParticipationUseCase;

    public ParticipationController(
            ParticipateInMatchUseCase participateInMatchUseCase,
            CancelParticipationUseCase cancelParticipationUseCase) {
        this.participateInMatchUseCase = participateInMatchUseCase;
        this.cancelParticipationUseCase = cancelParticipationUseCase;
    }

    /**
     * 경기 참가 신청 API
     *
     * @param matchId 참가할 경기 ID
     * @param userId 인증된 사용자 ID (JWT에서 추출)
     * @return 생성된 참가 정보 (201 Created)
     */
    @PostMapping("/{matchId}/participations")
    public ResponseEntity<ParticipationResponse> participateInMatch(
            @PathVariable("matchId") Long matchId,
            @AuthenticationPrincipal Long userId) {

        ParticipateInMatchCommand command = new ParticipateInMatchCommand(matchId, userId);
        Participation participation = participateInMatchUseCase.participateInMatch(command);

        return ResponseEntity.created(
                URI.create("/api/matches/" + matchId + "/participations/" + participation.getId()))
                .body(ParticipationResponse.of(participation));
    }

    /**
     * 경기 참가 취소 API
     *
     * @param matchId 경기 ID
     * @param participationId 취소할 참가 ID
     * @param userId 인증된 사용자 ID (JWT에서 추출)
     * @return 204 No Content
     */
    @DeleteMapping("/{matchId}/participations/{participationId}")
    public ResponseEntity<Void> cancelParticipation(
            @PathVariable("matchId") Long matchId,
            @PathVariable("participationId") Long participationId,
            @AuthenticationPrincipal Long userId) {

        CancelParticipationCommand command = new CancelParticipationCommand(
                matchId, participationId, userId);
        cancelParticipationUseCase.cancelParticipation(command);

        return ResponseEntity.noContent().build();
    }
}
