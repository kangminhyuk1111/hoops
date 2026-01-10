package com.hoops.participation.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hoops.match.application.port.out.MatchRepository;
import com.hoops.match.domain.Match;
import com.hoops.match.domain.MatchStatus;
import com.hoops.participation.application.exception.AlreadyParticipatingException;
import com.hoops.participation.application.exception.HostCannotParticipateException;
import com.hoops.participation.application.exception.InvalidMatchStatusException;
import com.hoops.participation.application.exception.MatchFullException;
import com.hoops.participation.application.exception.ParticipationMatchNotFoundException;
import com.hoops.participation.application.port.in.ParticipateInMatchCommand;
import com.hoops.participation.application.port.in.ParticipateInMatchUseCase;
import com.hoops.participation.domain.Participation;
import com.hoops.participation.domain.ParticipationStatus;
import com.hoops.participation.domain.repository.ParticipationRepository;
import com.hoops.support.IntegrationTestSupport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ParticipationServiceTest extends IntegrationTestSupport {

    @Autowired
    private ParticipateInMatchUseCase participateInMatchUseCase;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @BeforeEach
    void setUp() {
        cleanUpDatabase();
    }

    @Test
    @DisplayName("호스트가 자신의 경기에 참가 시 HostCannotParticipateException이 발생한다")
    void participateInMatch_throwsHostCannotParticipateException_whenHostTriesToParticipate() {
        // given
        Long hostId = 100L;
        Match savedMatch = matchRepository.save(createMatch(hostId, 5, 10, MatchStatus.PENDING));

        ParticipateInMatchCommand command = new ParticipateInMatchCommand(
                savedMatch.getId(),
                hostId  // 호스트가 자신의 경기에 참가 시도
        );

        // when & then
        assertThatThrownBy(() -> participateInMatchUseCase.participateInMatch(command))
                .isInstanceOf(HostCannotParticipateException.class)
                .hasMessageContaining("호스트는 자신의 경기에 참가");
    }

    @Test
    @DisplayName("정원이 가득 찬 경기에 참가 시 MatchFullException이 발생한다")
    void participateInMatch_throwsMatchFullException_whenMatchIsFull() {
        // given
        Long hostId = 100L;
        Long participantId = 200L;
        Match savedMatch = matchRepository.save(createMatch(hostId, 10, 10, MatchStatus.PENDING));

        ParticipateInMatchCommand command = new ParticipateInMatchCommand(
                savedMatch.getId(),
                participantId
        );

        // when & then
        assertThatThrownBy(() -> participateInMatchUseCase.participateInMatch(command))
                .isInstanceOf(MatchFullException.class)
                .hasMessageContaining("경기 정원이 모두 찼습니다");
    }

    @Test
    @DisplayName("CANCELLED 상태의 경기에 참가 시 InvalidMatchStatusException이 발생한다")
    void participateInMatch_throwsInvalidMatchStatusException_whenMatchIsCancelled() {
        // given
        Long hostId = 100L;
        Long participantId = 200L;
        Match savedMatch = matchRepository.save(createMatch(hostId, 5, 10, MatchStatus.CANCELLED));

        ParticipateInMatchCommand command = new ParticipateInMatchCommand(
                savedMatch.getId(),
                participantId
        );

        // when & then
        assertThatThrownBy(() -> participateInMatchUseCase.participateInMatch(command))
                .isInstanceOf(InvalidMatchStatusException.class)
                .hasMessageContaining("참가 신청할 수 없는 경기 상태입니다");
    }

    @Test
    @DisplayName("이미 참가한 경기에 중복 참가 시 AlreadyParticipatingException이 발생한다")
    void participateInMatch_throwsAlreadyParticipatingException_whenAlreadyParticipating() {
        // given
        Long hostId = 100L;
        Long participantId = 200L;
        Match savedMatch = matchRepository.save(createMatch(hostId, 5, 10, MatchStatus.PENDING));

        // 먼저 참가 정보를 저장
        Participation existingParticipation = new Participation(
                null,
                savedMatch.getId(),
                participantId,
                ParticipationStatus.CONFIRMED,
                LocalDateTime.now()
        );
        participationRepository.save(existingParticipation);

        ParticipateInMatchCommand command = new ParticipateInMatchCommand(
                savedMatch.getId(),
                participantId  // 이미 참가한 사용자가 다시 참가 시도
        );

        // when & then
        assertThatThrownBy(() -> participateInMatchUseCase.participateInMatch(command))
                .isInstanceOf(AlreadyParticipatingException.class)
                .hasMessageContaining("이미 참가 중인 경기입니다");
    }

    @Test
    @DisplayName("존재하지 않는 경기에 참가 시 ParticipationMatchNotFoundException이 발생한다")
    void participateInMatch_throwsParticipationMatchNotFoundException_whenMatchNotFound() {
        // given
        Long nonExistentMatchId = 99999L;
        Long participantId = 200L;

        ParticipateInMatchCommand command = new ParticipateInMatchCommand(
                nonExistentMatchId,
                participantId
        );

        // when & then
        assertThatThrownBy(() -> participateInMatchUseCase.participateInMatch(command))
                .isInstanceOf(ParticipationMatchNotFoundException.class)
                .hasMessageContaining("참가 신청할 경기를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("정상적으로 경기에 참가하면 CONFIRMED 상태의 Participation이 반환된다")
    void participateInMatch_returnsConfirmedParticipation_whenSuccessful() {
        // given
        Long hostId = 100L;
        Long participantId = 300L;
        Match savedMatch = matchRepository.save(createMatch(hostId, 5, 10, MatchStatus.PENDING));

        ParticipateInMatchCommand command = new ParticipateInMatchCommand(
                savedMatch.getId(),
                participantId
        );

        // when
        Participation result = participateInMatchUseCase.participateInMatch(command);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getMatchId()).isEqualTo(savedMatch.getId());
        assertThat(result.getUserId()).isEqualTo(participantId);
        assertThat(result.getStatus()).isEqualTo(ParticipationStatus.CONFIRMED);
        assertThat(result.getJoinedAt()).isNotNull();
    }

    @Test
    @DisplayName("정상적으로 경기에 참가하면 Match의 currentParticipants가 1 증가한다")
    void participateInMatch_incrementsCurrentParticipants_whenSuccessful() {
        // given
        Long hostId = 100L;
        Long participantId = 400L;
        int initialParticipants = 5;
        Match savedMatch = matchRepository.save(createMatch(hostId, initialParticipants, 10, MatchStatus.PENDING));

        ParticipateInMatchCommand command = new ParticipateInMatchCommand(
                savedMatch.getId(),
                participantId
        );

        // when
        participateInMatchUseCase.participateInMatch(command);

        // then
        Match updatedMatch = matchRepository.findById(savedMatch.getId()).orElseThrow();
        assertThat(updatedMatch.getCurrentParticipants()).isEqualTo(initialParticipants + 1);
    }

    @Test
    @DisplayName("마지막 자리에 참가하면 Match 상태가 FULL로 변경된다")
    void participateInMatch_changesMatchStatusToFull_whenLastSlotTaken() {
        // given
        Long hostId = 100L;
        Long participantId = 500L;
        int maxParticipants = 10;
        Match savedMatch = matchRepository.save(createMatch(hostId, maxParticipants - 1, maxParticipants, MatchStatus.PENDING));

        ParticipateInMatchCommand command = new ParticipateInMatchCommand(
                savedMatch.getId(),
                participantId
        );

        // when
        participateInMatchUseCase.participateInMatch(command);

        // then
        Match updatedMatch = matchRepository.findById(savedMatch.getId()).orElseThrow();
        assertThat(updatedMatch.getCurrentParticipants()).isEqualTo(maxParticipants);
        assertThat(updatedMatch.getStatus()).isEqualTo(MatchStatus.FULL);
    }

    @Test
    @DisplayName("동시에 여러 사용자가 참가 신청 시 낙관적 락 재시도로 정상 처리된다")
    void participateInMatch_handlesOptimisticLockWithRetry_whenConcurrentRequests() throws InterruptedException {
        // given
        Long hostId = 100L;
        int initialParticipants = 5;
        int maxParticipants = 10;
        int concurrentUsers = 3;  // 동시에 참가 시도하는 사용자 수
        Match savedMatch = matchRepository.save(createMatch(hostId, initialParticipants, maxParticipants, MatchStatus.PENDING));

        ExecutorService executorService = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch startLatch = new CountDownLatch(1);  // 동시 시작을 위한 래치
        CountDownLatch endLatch = new CountDownLatch(concurrentUsers);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < concurrentUsers; i++) {
            final Long participantId = 600L + i;
            executorService.submit(() -> {
                try {
                    startLatch.await();  // 모든 스레드가 동시에 시작하도록 대기
                    ParticipateInMatchCommand command = new ParticipateInMatchCommand(
                            savedMatch.getId(),
                            participantId
                    );
                    participateInMatchUseCase.participateInMatch(command);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    exceptions.add(e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();  // 모든 스레드 동시 시작
        endLatch.await();  // 모든 스레드 완료 대기
        executorService.shutdown();

        // then
        Match updatedMatch = matchRepository.findById(savedMatch.getId()).orElseThrow();

        // 재시도 로직으로 인해 모든 요청이 성공해야 함 (정원 내에서)
        assertThat(successCount.get()).isEqualTo(concurrentUsers);
        assertThat(failCount.get()).isZero();
        assertThat(updatedMatch.getCurrentParticipants()).isEqualTo(initialParticipants + concurrentUsers);
    }

    @Test
    @DisplayName("동시에 마지막 자리를 여러 사용자가 신청 시 한 명만 성공하고 나머지는 MatchFullException 발생")
    void participateInMatch_onlyOneSucceeds_whenConcurrentRequestsForLastSlot() throws InterruptedException {
        // given
        Long hostId = 100L;
        int maxParticipants = 10;
        int initialParticipants = maxParticipants - 1;  // 마지막 1자리만 남음
        int concurrentUsers = 5;  // 동시에 참가 시도하는 사용자 수
        Match savedMatch = matchRepository.save(createMatch(hostId, initialParticipants, maxParticipants, MatchStatus.PENDING));

        ExecutorService executorService = Executors.newFixedThreadPool(concurrentUsers);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(concurrentUsers);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger matchFullCount = new AtomicInteger(0);
        List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

        // when
        for (int i = 0; i < concurrentUsers; i++) {
            final Long participantId = 700L + i;
            executorService.submit(() -> {
                try {
                    startLatch.await();
                    ParticipateInMatchCommand command = new ParticipateInMatchCommand(
                            savedMatch.getId(),
                            participantId
                    );
                    participateInMatchUseCase.participateInMatch(command);
                    successCount.incrementAndGet();
                } catch (MatchFullException e) {
                    matchFullCount.incrementAndGet();
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    endLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        endLatch.await();
        executorService.shutdown();

        // then
        Match updatedMatch = matchRepository.findById(savedMatch.getId()).orElseThrow();

        // 정확히 1명만 성공, 나머지는 MatchFullException
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(matchFullCount.get()).isEqualTo(concurrentUsers - 1);
        assertThat(exceptions).isEmpty();  // 예상치 못한 예외 없음
        assertThat(updatedMatch.getCurrentParticipants()).isEqualTo(maxParticipants);
        assertThat(updatedMatch.getStatus()).isEqualTo(MatchStatus.FULL);
    }

    private Match createMatch(Long hostId, int currentParticipants, int maxParticipants, MatchStatus status) {
        return new Match(
                null,
                null,  // version - 새로 생성 시 null
                hostId,
                "테스트호스트",
                "테스트 경기",
                "설명",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780),
                "서울시 중구",
                LocalDate.now().plusDays(1),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                maxParticipants,
                currentParticipants,
                status
        );
    }
}
