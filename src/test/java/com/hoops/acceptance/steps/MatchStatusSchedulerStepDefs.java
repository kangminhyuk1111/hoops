package com.hoops.acceptance.steps;

import com.hoops.match.application.port.in.UpdateMatchStatusUseCase;
import com.hoops.match.application.port.out.MatchRepository;
import com.hoops.match.domain.Match;
import com.hoops.match.domain.MatchStatus;
import com.hoops.user.domain.User;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class MatchStatusSchedulerStepDefs {

    private final MatchRepository matchRepository;
    private final UpdateMatchStatusUseCase updateMatchStatusUseCase;
    private final SharedTestContext sharedContext;

    public MatchStatusSchedulerStepDefs(
            MatchRepository matchRepository,
            UpdateMatchStatusUseCase updateMatchStatusUseCase,
            SharedTestContext sharedContext) {
        this.matchRepository = matchRepository;
        this.updateMatchStatusUseCase = updateMatchStatusUseCase;
        this.sharedContext = sharedContext;
    }

    @먼저("시작 시간이 지난 PENDING 경기가 있다")
    public void 시작_시간이_지난_PENDING_경기가_있다() {
        createMatchWithStatus(MatchStatus.PENDING, true, false);
    }

    @먼저("종료 시간이 지난 IN_PROGRESS 경기가 있다")
    public void 종료_시간이_지난_IN_PROGRESS_경기가_있다() {
        createMatchWithStatus(MatchStatus.IN_PROGRESS, true, true);
    }

    @먼저("시작 시간이 지난 CANCELLED 경기가 있다")
    public void 시작_시간이_지난_CANCELLED_경기가_있다() {
        createMatchWithStatus(MatchStatus.CANCELLED, true, false);
    }

    @먼저("시작 시간이 되지 않은 PENDING 경기가 있다")
    public void 시작_시간이_되지_않은_PENDING_경기가_있다() {
        createMatchWithStatus(MatchStatus.PENDING, false, false);
    }

    @만일("경기 상태 업데이트가 실행된다")
    public void 경기_상태_업데이트가_실행된다() {
        updateMatchStatusUseCase.startMatches();
        updateMatchStatusUseCase.endMatches();
    }

    // "해당 경기의 상태가 {word} 이다" step은 CancelMatchStepDefs에 정의되어 있음

    private void createMatchWithStatus(MatchStatus status, boolean started, boolean ended) {
        User testUser = sharedContext.getTestUser();
        sharedContext.clearTestMatches();

        LocalDate matchDate;
        LocalTime startTime;
        LocalTime endTime;

        if (ended) {
            // 종료 시간이 지난 경기: 어제 오전 10시~12시
            matchDate = LocalDate.now().minusDays(1);
            startTime = LocalTime.of(10, 0);
            endTime = LocalTime.of(12, 0);
        } else if (started) {
            // 시작 시간이 지났지만 종료 시간은 안 지난 경기: 오늘 1시간 전 ~ 1시간 후
            matchDate = LocalDate.now();
            startTime = LocalTime.now().minusHours(1);
            endTime = LocalTime.now().plusHours(1);
        } else {
            // 아직 시작하지 않은 경기: 내일 오후 6시~8시
            matchDate = LocalDate.now().plusDays(1);
            startTime = LocalTime.of(18, 0);
            endTime = LocalTime.of(20, 0);
        }

        Match match = Match.builder()
                .version(0L)
                .hostId(testUser.getId())
                .hostNickname(testUser.getNickname())
                .title("스케줄러 테스트 경기")
                .description("테스트 경기 설명")
                .latitude(BigDecimal.valueOf(37.5665))
                .longitude(BigDecimal.valueOf(126.9780))
                .address("서울시 중구")
                .matchDate(matchDate)
                .startTime(startTime)
                .endTime(endTime)
                .maxParticipants(10)
                .currentParticipants(0)
                .status(status)
                .build();

        Match savedMatch = matchRepository.save(match);
        sharedContext.addTestMatch(savedMatch);
    }
}
