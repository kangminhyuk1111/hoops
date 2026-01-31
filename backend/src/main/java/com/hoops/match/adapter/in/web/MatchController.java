package com.hoops.match.adapter.in.web;

import com.hoops.match.adapter.in.web.dto.CancelMatchRequest;
import com.hoops.match.adapter.in.web.dto.CreateMatchRequest;
import com.hoops.match.adapter.in.web.dto.MatchListResponse;
import com.hoops.match.adapter.in.web.dto.MatchResponse;
import com.hoops.match.adapter.in.web.dto.UpdateMatchRequest;
import com.hoops.match.application.dto.MatchLocationQueryResult;
import com.hoops.match.application.port.in.CancelMatchCommand;
import com.hoops.match.application.port.in.CancelMatchUseCase;
import com.hoops.match.application.port.in.CreateMatchUseCase;
import com.hoops.match.application.port.in.MatchQueryUseCase;
import com.hoops.match.application.port.in.ReactivateMatchCommand;
import com.hoops.match.application.port.in.ReactivateMatchUseCase;
import com.hoops.match.application.port.in.UpdateMatchUseCase;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchSortType;
import com.hoops.match.domain.vo.MatchStatus;
import com.hoops.match.domain.vo.SearchDistance;
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
    private final ReactivateMatchUseCase reactivateMatchUseCase;

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
    public ResponseEntity<MatchResponse> getMatch(
            @Parameter(description = "경기 ID") @PathVariable("matchId") Long matchId) {
        Match match = matchQueryUseCase.getMatchById(matchId);
        return ResponseEntity.ok(MatchResponse.of(match));
    }

    @Operation(summary = "위치 기반 경기 목록 조회", description = "지정된 위치 반경 내의 경기 목록을 조회합니다. distance 미지정 시 기본 50km 반경으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    public ResponseEntity<MatchListResponse> getMatchesByLocation(
            @Parameter(description = "위도", example = "37.5665") @RequestParam BigDecimal latitude,
            @Parameter(description = "경도", example = "126.9780") @RequestParam BigDecimal longitude,
            @Parameter(description = "반경 (km, 허용: 1, 3, 5, 10)", example = "5") @RequestParam(required = false) Integer distance,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "경기 상태 필터", example = "PENDING") @RequestParam(required = false) MatchStatus status,
            @Parameter(description = "정렬 방식 (DISTANCE, URGENCY)", example = "DISTANCE") @RequestParam(defaultValue = "DISTANCE") String sort) {
        Double radiusKm = null;
        if (distance != null) {
            SearchDistance searchDistance = SearchDistance.from(distance);
            radiusKm = (double) searchDistance.getKm();
        }
        MatchSortType sortType = MatchSortType.from(sort);

        MatchLocationQueryResult result = matchQueryUseCase.getMatchesByLocation(
                latitude,
                longitude,
                radiusKm,
                page,
                size,
                status,
                sortType
        );

        List<MatchResponse> items = result.matches().stream()
                .map(mwd -> MatchResponse.of(mwd.match(), mwd.distanceKm()))
                .toList();

        return ResponseEntity.ok(new MatchListResponse(items, result.totalCount(), result.hasMore()));
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

    @Operation(summary = "경기 취소", description = "경기를 취소합니다. 호스트만 취소 가능합니다. 취소 사유를 반드시 입력해야 합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "취소 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (취소 사유 누락)"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (호스트가 아님)"),
            @ApiResponse(responseCode = "404", description = "경기를 찾을 수 없음")
    })
    @DeleteMapping("/{matchId}")
    public ResponseEntity<Void> cancelMatch(
            @Parameter(description = "경기 ID") @PathVariable("matchId") Long matchId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId,
            @RequestBody CancelMatchRequest request) {
        CancelMatchCommand command = request.toCommand(matchId, userId);
        cancelMatchUseCase.cancelMatch(command);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "내가 호스팅한 경기 목록 조회", description = "로그인한 사용자가 생성한 경기 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "401", description = "인증 필요")
    })
    @GetMapping("/hosted")
    public ResponseEntity<List<MatchResponse>> getMyHostedMatches(
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        List<Match> matches = matchQueryUseCase.getMyHostedMatches(userId);
        List<MatchResponse> response = matches.stream()
                .map(MatchResponse::of)
                .toList();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "경기 복구", description = "취소된 경기를 복구합니다. 호스트만 복구 가능하며, 취소 후 1시간 이내에만 복구할 수 있습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "복구 성공"),
            @ApiResponse(responseCode = "400", description = "복구 불가 (취소되지 않은 경기, 1시간 초과, 경기 날짜 지남)"),
            @ApiResponse(responseCode = "403", description = "권한 없음 (호스트가 아님)"),
            @ApiResponse(responseCode = "404", description = "경기를 찾을 수 없음")
    })
    @PostMapping("/{matchId}/reactivate")
    public ResponseEntity<MatchResponse> reactivateMatch(
            @Parameter(description = "경기 ID") @PathVariable("matchId") Long matchId,
            @Parameter(hidden = true) @AuthenticationPrincipal Long userId) {
        ReactivateMatchCommand command = new ReactivateMatchCommand(matchId, userId);
        reactivateMatchUseCase.reactivateMatch(command);
        Match match = matchQueryUseCase.getMatchById(matchId);
        return ResponseEntity.ok(MatchResponse.of(match));
    }
}
