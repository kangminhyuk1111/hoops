package com.hoops.match.adapter.in.web;

import com.hoops.match.adapter.in.web.dto.MatchResponse;
import com.hoops.match.application.port.in.MatchQueryUseCase;
import com.hoops.match.domain.Match;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

    /**
     * 생성자 주입을 통한 의존성 주입
     *
     * @param matchQueryUseCase 경기 조회 Use Case
     */
    public MatchController(MatchQueryUseCase matchQueryUseCase) {
        this.matchQueryUseCase = matchQueryUseCase;
    }

    /**
     * 위치 기반 경기 목록 조회 API
     *
     * 사용자의 현재 위치(위도, 경도)와 검색 반경(km)을 기준으로
     * 근처의 경기 목록을 조회합니다.
     *
     * @param latitude 위도 (예: 37.4980)
     * @param longitude 경도 (예: 127.0270)
     * @param distance 검색 반경 (km 단위, 예: 5)
     * @return 경기 목록 응답
     */
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
                .map(MatchResponse::from)
                .toList();

        return ResponseEntity.ok(response);
    }
}
