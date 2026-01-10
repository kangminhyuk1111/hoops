package com.hoops.participation.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.hoops.match.application.port.out.MatchRepository;
import com.hoops.match.domain.Match;
import com.hoops.match.domain.MatchStatus;
import com.hoops.participation.application.exception.InvalidParticipationStatusException;
import com.hoops.participation.application.exception.MatchAlreadyStartedException;
import com.hoops.participation.application.exception.NotParticipantException;
import com.hoops.participation.application.exception.ParticipationNotFoundException;
import com.hoops.participation.application.port.in.CancelParticipationCommand;
import com.hoops.participation.application.port.in.CancelParticipationUseCase;
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
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 참가 취소 기능 통합 테스트
 */
class ParticipationCancellationTest extends IntegrationTestSupport {

    @Autowired
    private CancelParticipationUseCase cancelParticipationUseCase;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ParticipationRepository participationRepository;

    @BeforeEach
    void setUp() {
        cleanUpDatabase();
    }

    @Nested
    @DisplayName("정상 취소 케이스")
    class SuccessfulCancellationTest {

        @Test
        @DisplayName("정상적으로 참가 취소 시 Participation 상태가 CANCELLED로 변경된다")
        void cancelParticipation_changesStatusToCancelled_whenSuccessful() {
            // given
            Long hostId = 100L;
            Long participantId = 200L;
            Match savedMatch = matchRepository.save(createFutureMatch(hostId, 5, 10, MatchStatus.PENDING));

            Participation participation = participationRepository.save(
                    createConfirmedParticipation(savedMatch.getId(), participantId));

            CancelParticipationCommand command = new CancelParticipationCommand(
                    savedMatch.getId(),
                    participation.getId(),
                    participantId
            );

            // when
            cancelParticipationUseCase.cancelParticipation(command);

            // then
            Participation cancelledParticipation = participationRepository.findById(participation.getId())
                    .orElseThrow();
            assertThat(cancelledParticipation.getStatus()).isEqualTo(ParticipationStatus.CANCELLED);
        }

        @Test
        @DisplayName("참가 취소 시 Match의 currentParticipants가 1 감소한다")
        void cancelParticipation_decrementsCurrentParticipants_whenSuccessful() {
            // given
            Long hostId = 100L;
            Long participantId = 200L;
            int initialParticipants = 5;
            Match savedMatch = matchRepository.save(
                    createFutureMatch(hostId, initialParticipants, 10, MatchStatus.PENDING));

            Participation participation = participationRepository.save(
                    createConfirmedParticipation(savedMatch.getId(), participantId));

            CancelParticipationCommand command = new CancelParticipationCommand(
                    savedMatch.getId(),
                    participation.getId(),
                    participantId
            );

            // when
            cancelParticipationUseCase.cancelParticipation(command);

            // then
            Match updatedMatch = matchRepository.findById(savedMatch.getId()).orElseThrow();
            assertThat(updatedMatch.getCurrentParticipants()).isEqualTo(initialParticipants - 1);
        }

        @Test
        @DisplayName("FULL 상태 경기에서 참가 취소 시 Match 상태가 PENDING으로 변경된다")
        void cancelParticipation_changesMatchStatusToPending_whenWasFull() {
            // given
            Long hostId = 100L;
            Long participantId = 200L;
            int maxParticipants = 10;
            Match savedMatch = matchRepository.save(
                    createFutureMatch(hostId, maxParticipants, maxParticipants, MatchStatus.FULL));

            Participation participation = participationRepository.save(
                    createConfirmedParticipation(savedMatch.getId(), participantId));

            CancelParticipationCommand command = new CancelParticipationCommand(
                    savedMatch.getId(),
                    participation.getId(),
                    participantId
            );

            // when
            cancelParticipationUseCase.cancelParticipation(command);

            // then
            Match updatedMatch = matchRepository.findById(savedMatch.getId()).orElseThrow();
            assertThat(updatedMatch.getCurrentParticipants()).isEqualTo(maxParticipants - 1);
            assertThat(updatedMatch.getStatus()).isEqualTo(MatchStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("예외 케이스")
    class ExceptionCaseTest {

        @Test
        @DisplayName("존재하지 않는 참가 ID로 취소 시 ParticipationNotFoundException이 발생한다")
        void cancelParticipation_throwsParticipationNotFoundException_whenNotFound() {
            // given
            Long nonExistentParticipationId = 99999L;
            Long matchId = 1L;
            Long userId = 200L;

            CancelParticipationCommand command = new CancelParticipationCommand(
                    matchId,
                    nonExistentParticipationId,
                    userId
            );

            // when & then
            assertThatThrownBy(() -> cancelParticipationUseCase.cancelParticipation(command))
                    .isInstanceOf(ParticipationNotFoundException.class)
                    .hasMessageContaining("참가 정보를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("본인이 아닌 사용자가 취소 시도 시 NotParticipantException이 발생한다")
        void cancelParticipation_throwsNotParticipantException_whenNotOwner() {
            // given
            Long hostId = 100L;
            Long participantId = 200L;
            Long differentUserId = 999L;  // 다른 사용자
            Match savedMatch = matchRepository.save(createFutureMatch(hostId, 5, 10, MatchStatus.PENDING));

            Participation participation = participationRepository.save(
                    createConfirmedParticipation(savedMatch.getId(), participantId));

            CancelParticipationCommand command = new CancelParticipationCommand(
                    savedMatch.getId(),
                    participation.getId(),
                    differentUserId  // 다른 사용자가 취소 시도
            );

            // when & then
            assertThatThrownBy(() -> cancelParticipationUseCase.cancelParticipation(command))
                    .isInstanceOf(NotParticipantException.class)
                    .hasMessageContaining("본인의 참가만 취소할 수 있습니다");
        }

        @Test
        @DisplayName("이미 취소된 참가를 다시 취소 시 InvalidParticipationStatusException이 발생한다")
        void cancelParticipation_throwsInvalidParticipationStatusException_whenAlreadyCancelled() {
            // given
            Long hostId = 100L;
            Long participantId = 200L;
            Match savedMatch = matchRepository.save(createFutureMatch(hostId, 5, 10, MatchStatus.PENDING));

            Participation cancelledParticipation = participationRepository.save(
                    createCancelledParticipation(savedMatch.getId(), participantId));

            CancelParticipationCommand command = new CancelParticipationCommand(
                    savedMatch.getId(),
                    cancelledParticipation.getId(),
                    participantId
            );

            // when & then
            assertThatThrownBy(() -> cancelParticipationUseCase.cancelParticipation(command))
                    .isInstanceOf(InvalidParticipationStatusException.class)
                    .hasMessageContaining("취소할 수 없는 참가 상태입니다");
        }

        @Test
        @DisplayName("PENDING 상태의 참가를 취소 시 InvalidParticipationStatusException이 발생한다")
        void cancelParticipation_throwsInvalidParticipationStatusException_whenPending() {
            // given
            Long hostId = 100L;
            Long participantId = 200L;
            Match savedMatch = matchRepository.save(createFutureMatch(hostId, 5, 10, MatchStatus.PENDING));

            Participation pendingParticipation = participationRepository.save(
                    createPendingParticipation(savedMatch.getId(), participantId));

            CancelParticipationCommand command = new CancelParticipationCommand(
                    savedMatch.getId(),
                    pendingParticipation.getId(),
                    participantId
            );

            // when & then
            assertThatThrownBy(() -> cancelParticipationUseCase.cancelParticipation(command))
                    .isInstanceOf(InvalidParticipationStatusException.class)
                    .hasMessageContaining("취소할 수 없는 참가 상태입니다");
        }

        @Test
        @DisplayName("이미 시작된 경기의 참가를 취소 시 MatchAlreadyStartedException이 발생한다")
        void cancelParticipation_throwsMatchAlreadyStartedException_whenMatchStarted() {
            // given
            Long hostId = 100L;
            Long participantId = 200L;
            // 과거 날짜의 경기 (이미 시작됨)
            Match savedMatch = matchRepository.save(createPastMatch(hostId, 5, 10, MatchStatus.PENDING));

            Participation participation = participationRepository.save(
                    createConfirmedParticipation(savedMatch.getId(), participantId));

            CancelParticipationCommand command = new CancelParticipationCommand(
                    savedMatch.getId(),
                    participation.getId(),
                    participantId
            );

            // when & then
            assertThatThrownBy(() -> cancelParticipationUseCase.cancelParticipation(command))
                    .isInstanceOf(MatchAlreadyStartedException.class)
                    .hasMessageContaining("이미 시작된 경기는 참가를 취소할 수 없습니다");
        }
    }

    @Nested
    @DisplayName("동시성 테스트")
    class ConcurrencyTest {

        @Test
        @DisplayName("동시에 여러 사용자가 참가 취소 시 낙관적 락 재시도로 정상 처리된다")
        void cancelParticipation_handlesOptimisticLockWithRetry_whenConcurrentRequests() throws InterruptedException {
            // given
            Long hostId = 100L;
            int initialParticipants = 5;
            int concurrentCancellations = 3;
            Match savedMatch = matchRepository.save(
                    createFutureMatch(hostId, initialParticipants, 10, MatchStatus.PENDING));

            // 여러 참가자 생성
            List<Participation> participations = new ArrayList<>();
            for (int i = 0; i < concurrentCancellations; i++) {
                Long participantId = 200L + i;
                Participation participation = participationRepository.save(
                        createConfirmedParticipation(savedMatch.getId(), participantId));
                participations.add(participation);
            }

            ExecutorService executorService = Executors.newFixedThreadPool(concurrentCancellations);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(concurrentCancellations);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger failCount = new AtomicInteger(0);
            List<Exception> exceptions = Collections.synchronizedList(new ArrayList<>());

            // when
            for (int i = 0; i < concurrentCancellations; i++) {
                final int index = i;
                final Participation participation = participations.get(i);
                final Long participantId = 200L + i;

                executorService.submit(() -> {
                    try {
                        startLatch.await();
                        CancelParticipationCommand command = new CancelParticipationCommand(
                                savedMatch.getId(),
                                participation.getId(),
                                participantId
                        );
                        cancelParticipationUseCase.cancelParticipation(command);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failCount.incrementAndGet();
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

            // 재시도 로직으로 인해 모든 취소 요청이 성공해야 함
            assertThat(successCount.get()).isEqualTo(concurrentCancellations);
            assertThat(failCount.get()).isZero();
            assertThat(exceptions).isEmpty();
            assertThat(updatedMatch.getCurrentParticipants())
                    .isEqualTo(initialParticipants - concurrentCancellations);

            // 모든 참가가 CANCELLED 상태로 변경되었는지 확인
            for (Participation participation : participations) {
                Participation updated = participationRepository.findById(participation.getId()).orElseThrow();
                assertThat(updated.getStatus()).isEqualTo(ParticipationStatus.CANCELLED);
            }
        }

        @Test
        @DisplayName("같은 참가를 동시에 취소 시도하면 한 번만 성공한다")
        void cancelParticipation_onlyOneSucceeds_whenSameParticipationCancelledConcurrently() throws InterruptedException {
            // given
            Long hostId = 100L;
            Long participantId = 200L;
            int concurrentAttempts = 5;
            Match savedMatch = matchRepository.save(createFutureMatch(hostId, 5, 10, MatchStatus.PENDING));

            Participation participation = participationRepository.save(
                    createConfirmedParticipation(savedMatch.getId(), participantId));

            ExecutorService executorService = Executors.newFixedThreadPool(concurrentAttempts);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch endLatch = new CountDownLatch(concurrentAttempts);

            AtomicInteger successCount = new AtomicInteger(0);
            AtomicInteger invalidStatusCount = new AtomicInteger(0);
            List<Exception> otherExceptions = Collections.synchronizedList(new ArrayList<>());

            // when - 같은 참가를 동시에 여러 번 취소 시도
            for (int i = 0; i < concurrentAttempts; i++) {
                executorService.submit(() -> {
                    try {
                        startLatch.await();
                        CancelParticipationCommand command = new CancelParticipationCommand(
                                savedMatch.getId(),
                                participation.getId(),
                                participantId
                        );
                        cancelParticipationUseCase.cancelParticipation(command);
                        successCount.incrementAndGet();
                    } catch (InvalidParticipationStatusException e) {
                        invalidStatusCount.incrementAndGet();
                    } catch (Exception e) {
                        otherExceptions.add(e);
                    } finally {
                        endLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            endLatch.await();
            executorService.shutdown();

            // then
            // 정확히 1번만 성공, 나머지는 InvalidParticipationStatusException
            assertThat(successCount.get()).isEqualTo(1);
            assertThat(invalidStatusCount.get()).isEqualTo(concurrentAttempts - 1);
            assertThat(otherExceptions).isEmpty();

            Participation updated = participationRepository.findById(participation.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(ParticipationStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryTest {

        @Test
        @DisplayName("참가자가 1명인 경기에서 취소 시 currentParticipants가 0이 된다")
        void cancelParticipation_setsParticipantsToZero_whenOnlyOneParticipant() {
            // given
            Long hostId = 100L;
            Long participantId = 200L;
            Match savedMatch = matchRepository.save(createFutureMatch(hostId, 1, 10, MatchStatus.PENDING));

            Participation participation = participationRepository.save(
                    createConfirmedParticipation(savedMatch.getId(), participantId));

            CancelParticipationCommand command = new CancelParticipationCommand(
                    savedMatch.getId(),
                    participation.getId(),
                    participantId
            );

            // when
            cancelParticipationUseCase.cancelParticipation(command);

            // then
            Match updatedMatch = matchRepository.findById(savedMatch.getId()).orElseThrow();
            assertThat(updatedMatch.getCurrentParticipants()).isZero();
        }

        @Test
        @DisplayName("오늘 경기인데 시작 시간 전이면 취소가 가능하다")
        void cancelParticipation_succeeds_whenTodayButNotStartedYet() {
            // given
            Long hostId = 100L;
            Long participantId = 200L;
            // 오늘 경기지만 아직 시작 시간이 안 됨 (2시간 후)
            Match savedMatch = matchRepository.save(
                    createTodayFutureMatch(hostId, 5, 10, MatchStatus.PENDING));

            Participation participation = participationRepository.save(
                    createConfirmedParticipation(savedMatch.getId(), participantId));

            CancelParticipationCommand command = new CancelParticipationCommand(
                    savedMatch.getId(),
                    participation.getId(),
                    participantId
            );

            // when
            cancelParticipationUseCase.cancelParticipation(command);

            // then
            Participation cancelled = participationRepository.findById(participation.getId()).orElseThrow();
            assertThat(cancelled.getStatus()).isEqualTo(ParticipationStatus.CANCELLED);
        }
    }

    // ===================== Helper Methods =====================

    private Match createFutureMatch(Long hostId, int currentParticipants, int maxParticipants, MatchStatus status) {
        return new Match(
                null,
                null,
                hostId,
                "테스트호스트",
                "테스트 경기",
                "설명",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780),
                "서울시 중구",
                LocalDate.now().plusDays(1),  // 내일 경기
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                maxParticipants,
                currentParticipants,
                status
        );
    }

    private Match createPastMatch(Long hostId, int currentParticipants, int maxParticipants, MatchStatus status) {
        return new Match(
                null,
                null,
                hostId,
                "테스트호스트",
                "테스트 경기",
                "설명",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780),
                "서울시 중구",
                LocalDate.now().minusDays(1),  // 어제 경기 (이미 시작됨)
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                maxParticipants,
                currentParticipants,
                status
        );
    }

    private Match createTodayFutureMatch(Long hostId, int currentParticipants, int maxParticipants, MatchStatus status) {
        return new Match(
                null,
                null,
                hostId,
                "테스트호스트",
                "테스트 경기",
                "설명",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780),
                "서울시 중구",
                LocalDate.now(),  // 오늘 경기
                LocalTime.now().plusHours(2),  // 2시간 후 시작
                LocalTime.now().plusHours(4),
                maxParticipants,
                currentParticipants,
                status
        );
    }

    private Participation createConfirmedParticipation(Long matchId, Long userId) {
        return new Participation(
                null,
                matchId,
                userId,
                ParticipationStatus.CONFIRMED,
                LocalDateTime.now()
        );
    }

    private Participation createPendingParticipation(Long matchId, Long userId) {
        return new Participation(
                null,
                matchId,
                userId,
                ParticipationStatus.PENDING,
                LocalDateTime.now()
        );
    }

    private Participation createCancelledParticipation(Long matchId, Long userId) {
        return new Participation(
                null,
                matchId,
                userId,
                ParticipationStatus.CANCELLED,
                LocalDateTime.now()
        );
    }
}
