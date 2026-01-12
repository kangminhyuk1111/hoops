package com.hoops.match.application.scheduler;

import com.hoops.match.application.port.in.UpdateMatchStatusUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MatchStatusScheduler {

    private final UpdateMatchStatusUseCase updateMatchStatusUseCase;

    @Scheduled(fixedRate = 60000) // 1분마다 실행
    public void updateMatchStatuses() {
        log.debug("경기 상태 업데이트 스케줄러 실행");

        int startedCount = updateMatchStatusUseCase.startMatches();
        int endedCount = updateMatchStatusUseCase.endMatches();

        if (startedCount > 0 || endedCount > 0) {
            log.info("경기 상태 업데이트 완료: 시작된 경기 {}건, 종료된 경기 {}건", startedCount, endedCount);
        }
    }
}
