package com.hoops.match.adapter.in.web;

import com.hoops.match.adapter.dto.CreateMatchRequest;
import com.hoops.match.adapter.dto.MatchResponse;
import com.hoops.match.adapter.dto.UpdateMatchRequest;
import com.hoops.match.application.port.in.CancelMatchCommand;
import com.hoops.match.application.port.in.CancelMatchUseCase;
import com.hoops.match.application.port.in.CreateMatchUseCase;
import com.hoops.match.application.port.in.MatchQueryUseCase;
import com.hoops.match.application.port.in.UpdateMatchUseCase;
import com.hoops.match.domain.Match;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchQueryUseCase matchQueryUseCase;
    private final CreateMatchUseCase createMatchUseCase;
    private final UpdateMatchUseCase updateMatchUseCase;
    private final CancelMatchUseCase cancelMatchUseCase;

    @PostMapping
    public ResponseEntity<MatchResponse> createMatch(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateMatchRequest request) {
        Match match = createMatchUseCase.createMatch(request.toCommand(userId));

        return ResponseEntity.created(URI.create("/api/matches/" + match.getId()))
                .body(MatchResponse.of(match));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<MatchResponse> findMatchById(@PathVariable("matchId") Long matchId) {
        Match match = matchQueryUseCase.findMatchById(matchId);
        return ResponseEntity.ok(MatchResponse.of(match));
    }

    @GetMapping
    public ResponseEntity<List<MatchResponse>> findMatchesByLocation(
            @RequestParam BigDecimal latitude,
            @RequestParam BigDecimal longitude,
            @RequestParam BigDecimal distance) {
        BigDecimal distanceInMeters = distance.multiply(BigDecimal.valueOf(1000));

        List<Match> matches = matchQueryUseCase.loadMatchesByLocation(
                latitude,
                longitude,
                distanceInMeters
        );

        List<MatchResponse> response = matches.stream()
                .map(MatchResponse::of)
                .toList();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{matchId}")
    public ResponseEntity<MatchResponse> updateMatch(
            @PathVariable("matchId") Long matchId,
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateMatchRequest request) {
        Match match = updateMatchUseCase.updateMatch(request.toCommand(matchId, userId));
        return ResponseEntity.ok(MatchResponse.of(match));
    }

    @DeleteMapping("/{matchId}")
    public ResponseEntity<Void> cancelMatch(
            @PathVariable("matchId") Long matchId,
            @AuthenticationPrincipal Long userId) {
        CancelMatchCommand command = new CancelMatchCommand(matchId, userId);
        cancelMatchUseCase.cancelMatch(command);
        return ResponseEntity.noContent().build();
    }
}
