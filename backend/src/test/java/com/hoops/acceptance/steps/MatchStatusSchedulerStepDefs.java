package com.hoops.acceptance.steps;

import com.hoops.match.application.port.in.UpdateMatchStatusUseCase;
import com.hoops.match.domain.repository.MatchRepository;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchStatus;
import com.hoops.user.domain.model.User;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchStatusSchedulerStepDefs {

    private final MatchRepository matchRepository;
    private final UpdateMatchStatusUseCase updateMatchStatusUseCase;
    private final SharedTestContext sharedContext;

    private int totalStatusChangeCount = 0;

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
        int startedCount = updateMatchStatusUseCase.startMatches();
        int endedCount = updateMatchStatusUseCase.endMatches();
        totalStatusChangeCount += startedCount + endedCount;
    }

    @만일("경기 상태 업데이트가 다시 실행된다")
    public void 경기_상태_업데이트가_다시_실행된다() {
        int startedCount = updateMatchStatusUseCase.startMatches();
        int endedCount = updateMatchStatusUseCase.endMatches();
        totalStatusChangeCount += startedCount + endedCount;
    }

    @그리고("경기 상태 변경 횟수가 {int}회 이다")
    public void 경기_상태_변경_횟수가_N회_이다(int expectedCount) {
        assertThat(totalStatusChangeCount)
                .as("멱등성 검증: 상태 변경은 최초 1회만 발생해야 함")
                .isEqualTo(expectedCount);
    }

    // "해당 경기의 상태가 {word} 이다" step은 CancelMatchStepDefs에 정의되어 있음

    private void createMatchWithStatus(MatchStatus status, boolean started, boolean ended) {
        User testUser = sharedContext.getTestUser();
        sharedContext.clearTestMatches();
        totalStatusChangeCount = 0;  // 시나리오 시작 시 카운트 초기화

        LocalDate matchDate;
        LocalTime startTime;
        LocalTime endTime;

        if (ended) {
            // 종료 시간이 지난 경기: 어제 오전 10시~12시
            matchDate = LocalDate.now().minusDays(1);
            startTime = LocalTime.of(10, 0);
            endTime = LocalTime.of(12, 0);
        } else if (started) {
            // 시작 시간이 지났지만 종료 시간은 안 지난 경기
            // Note: LocalTime.now().plusHours(1)은 자정 근처에서 wrap되어 버그 발생
            // 고정된 시간 사용: 오늘 00:01 ~ 23:59 (startTime은 이미 지남, endTime은 아직 안 지남)
            matchDate = LocalDate.now();
            startTime = LocalTime.of(0, 1);
            endTime = LocalTime.of(23, 59);
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
