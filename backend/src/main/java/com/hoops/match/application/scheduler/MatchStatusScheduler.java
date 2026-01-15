package com.hoops.match.application.scheduler;

import com.hoops.match.application.port.in.UpdateMatchStatusUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 경기 상태 자동 업데이트 스케줄러
 *
 * 분산 환경에서 ShedLock을 통해 단일 인스턴스만 실행되도록 보장한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MatchStatusScheduler {

    private final UpdateMatchStatusUseCase updateMatchStatusUseCase;

    /**
     * 경기 상태 자동 업데이트
     *
     * - 시작 시간이 지난 경기: PENDING/CONFIRMED/FULL → IN_PROGRESS
     * - 종료 시간이 지난 경기: IN_PROGRESS → ENDED
     *
     * lockAtLeastFor: 최소 50초 락 유지 (60초 주기보다 짧게 설정하여 중복 실행 방지)
     * lockAtMostFor: 최대 5분 (장애 시 자동 해제)
     */
    @Scheduled(fixedRate = 60000) // 1분마다 실행
    @SchedulerLock(
            name = "updateMatchStatuses",
            lockAtLeastFor = "PT50S",
            lockAtMostFor = "PT5M"
    )
    public void updateMatchStatuses() {
        log.debug("경기 상태 업데이트 스케줄러 실행");

        int startedCount = updateMatchStatusUseCase.startMatches();
        int endedCount = updateMatchStatusUseCase.endMatches();

        if (startedCount > 0 || endedCount > 0) {
            log.info("경기 상태 업데이트 완료: 시작된 경기 {}건, 종료된 경기 {}건", startedCount, endedCount);
        }
    }
}
