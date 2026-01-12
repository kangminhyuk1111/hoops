package com.hoops.participation.adapter.in.web;

import com.hoops.participation.adapter.in.web.dto.ParticipationResponse;
import com.hoops.participation.application.port.in.CancelParticipationCommand;
import com.hoops.participation.application.port.in.CancelParticipationUseCase;
import com.hoops.participation.application.port.in.ParticipateInMatchCommand;
import com.hoops.participation.application.port.in.ParticipateInMatchUseCase;
import com.hoops.participation.domain.Participation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class ParticipationController {

    private final ParticipateInMatchUseCase participateInMatchUseCase;
    private final CancelParticipationUseCase cancelParticipationUseCase;

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
