package com.hoops.match.adapter.in.web;

import com.hoops.match.adapter.dto.CreateMatchRequest;
import com.hoops.match.adapter.dto.MatchResponse;
import com.hoops.match.application.port.in.CreateMatchUseCase;
import com.hoops.match.application.port.in.MatchQueryUseCase;
import com.hoops.match.domain.Match;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 경기 관련 REST API Controller
 *
 * Hexagonal Architecture의 Inbound Adapter로서,
 * HTTP 요청을 받아 UseCase를 호출하고 응답을 반환합니다.
 */
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchQueryUseCase matchQueryUseCase;
    private final CreateMatchUseCase createMatchUseCase;

    public MatchController(
            MatchQueryUseCase matchQueryUseCase,
            CreateMatchUseCase createMatchUseCase) {
        this.matchQueryUseCase = matchQueryUseCase;
        this.createMatchUseCase = createMatchUseCase;
    }

    /**
     * 경기 생성 API
     * @param userId 인증된 사용자 ID (JWT에서 추출)
     * @param request 경기 생성 요청 DTO
     * @return 생성된 경기 응답 (201 Created)
     */
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
            @RequestParam BigDecimal distance
    ) {
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
}
