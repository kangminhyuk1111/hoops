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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

@Tag(name = "Match", description = "경기 관련 API")
@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchQueryUseCase matchQueryUseCase;
    private final CreateMatchUseCase createMatchUseCase;
    private final UpdateMatchUseCase updateMatchUseCase;
    private final CancelMatchUseCase cancelMatchUseCase;

    @Operation(summary = "경기 생성", description = "새로운 경기를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "경기 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @PostMapping
    public ResponseEntity<MatchResponse> createMatch(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CreateMatchRequest request) {
        Match match = createMatchUseCase.createMatch(request.toCommand(userId));

        return ResponseEntity.created(URI.create("/api/matches/" + match.getId()))
                .body(MatchResponse.of(match));
    }

    @Operation(summary = "경기 상세 조회", description = "경기 ID로 경기 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "경기를 찾을 수 없음")
    })
    @GetMapping("/{matchId}")
    public ResponseEntity<MatchResponse> findMatchById(
            @Parameter(description = "경기 ID") @PathVariable("matchId") Long matchId) {
        Match match = matchQueryUseCase.findMatchById(matchId);
        return ResponseEntity.ok(MatchResponse.of(match));
    }

    @Operation(summary = "위치 기반 경기 목록 조회", description = "지정된 위치 반경 내의 경기 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<List<MatchResponse>> findMatchesByLocation(
            @Parameter(description = "위도", example = "37.5665") @RequestParam BigDecimal latitude,
            @Parameter(description = "경도", example = "126.9780") @RequestParam BigDecimal longitude,
            @Parameter(description = "반경 (km)", example = "5") @RequestParam BigDecimal distance) {
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

    @Operation(summary = "경기 수정", description = "경기 정보를 수정합니다. 호스트만 수정 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (호스트가 아님)"),
            @ApiResponse(responseCode = "404", description = "경기를 찾을 수 없음")
    })
    @PutMapping("/{matchId}")
    public ResponseEntity<MatchResponse> updateMatch(
            @Parameter(description = "경기 ID") @PathVariable("matchId") Long matchId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateMatchRequest request) {
        Match match = updateMatchUseCase.updateMatch(request.toCommand(matchId, userId));
        return ResponseEntity.ok(MatchResponse.of(match));
    }

    @Operation(summary = "경기 취소", description = "경기를 취소합니다. 호스트만 취소 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "취소 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (호스트가 아님)"),
            @ApiResponse(responseCode = "404", description = "경기를 찾을 수 없음")
    })
    @DeleteMapping("/{matchId}")
    public ResponseEntity<Void> cancelMatch(
            @Parameter(description = "경기 ID") @PathVariable("matchId") Long matchId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        CancelMatchCommand command = new CancelMatchCommand(matchId, userId);
        cancelMatchUseCase.cancelMatch(command);
        return ResponseEntity.noContent().build();
    }
}
