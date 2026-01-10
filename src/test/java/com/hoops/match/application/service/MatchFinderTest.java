package com.hoops.match.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hoops.match.application.exception.MatchNotFoundException;
import com.hoops.match.application.port.out.MatchRepository;
import com.hoops.match.domain.Match;
import com.hoops.match.domain.MatchStatus;
import com.hoops.support.IntegrationTestSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MatchFinderTest extends IntegrationTestSupport {

    @Autowired
    private MatchFinder matchFinder;

    @Autowired
    private MatchRepository matchRepository;

    @BeforeEach
    void setUp() {
        cleanUpDatabase();
    }

    @Test
    @DisplayName("findMatchById - 존재하는 매치 ID로 조회하면 매치를 반환한다")
    void findMatchById_returnsMatch_whenMatchExists() {
        // given
        Match saved = matchRepository.save(createMatch(
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780)
        ));

        // when
        Match found = matchFinder.findMatchById(saved.getId());

        // then
        assertThat(found.getId()).isEqualTo(saved.getId());
        assertThat(found.getTitle()).isEqualTo("테스트 경기");
    }

    @Test
    @DisplayName("findMatchById - 존재하지 않는 매치 ID로 조회하면 MatchNotFoundException 발생")
    void findMatchById_throwsException_whenMatchNotExists() {
        // given
        Long nonExistentId = 99999L;

        // when & then
        assertThatThrownBy(() -> matchFinder.findMatchById(nonExistentId))
                .isInstanceOf(MatchNotFoundException.class);
    }

    @Test
    @DisplayName("loadMatchesByLocation - 거리 내 매치를 반환한다")
    void loadMatchesByLocation_returnsMatches_withinDistance() {
        // given
        BigDecimal centerLat = BigDecimal.valueOf(37.5665);
        BigDecimal centerLon = BigDecimal.valueOf(126.9780);

        // 중심에서 약 500m 떨어진 위치
        matchRepository.save(createMatch(
                BigDecimal.valueOf(37.5700),
                BigDecimal.valueOf(126.9780)
        ));

        // when - 1km(1000m) 반경 내 조회
        List<Match> matches = matchFinder.loadMatchesByLocation(
                centerLat,
                centerLon,
                BigDecimal.valueOf(1000)
        );

        // then
        assertThat(matches).hasSize(1);
    }

    @Test
    @DisplayName("loadMatchesByLocation - 거리 밖 매치는 반환하지 않는다")
    void loadMatchesByLocation_excludesMatches_outsideDistance() {
        // given
        BigDecimal centerLat = BigDecimal.valueOf(37.5665);
        BigDecimal centerLon = BigDecimal.valueOf(126.9780);

        // 중심에서 약 5km 떨어진 위치
        matchRepository.save(createMatch(
                BigDecimal.valueOf(37.6100),
                BigDecimal.valueOf(126.9780)
        ));

        // when - 1km(1000m) 반경 내 조회
        List<Match> matches = matchFinder.loadMatchesByLocation(
                centerLat,
                centerLon,
                BigDecimal.valueOf(1000)
        );

        // then
        assertThat(matches).isEmpty();
    }

    private Match createMatch(BigDecimal latitude, BigDecimal longitude) {
        return new Match(
                null,
                null,  // version - 새로 생성 시 null
                1L,
                "테스트호스트",
                "테스트 경기",
                "설명",
                latitude,
                longitude,
                "서울시 중구",
                LocalDate.now().plusDays(1),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                4,
                1,
                MatchStatus.PENDING
        );
    }
}
