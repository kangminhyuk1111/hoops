package com.hoops.match.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MatchTest {

    @Test
    @DisplayName("addParticipant - 참가자 추가 시 currentParticipants가 1 증가한다")
    void addParticipant_increasesCurrentParticipants() {
        // given
        Match match = createMatch(1, 4, MatchStatus.PENDING);

        // when
        match.addParticipant();

        // then
        assertThat(match.getCurrentParticipants()).isEqualTo(2);
    }

    @Test
    @DisplayName("addParticipant - 정원이 다 차면 상태가 FULL로 변경된다")
    void addParticipant_changesStatusToFull_whenCapacityReached() {
        // given
        Match match = createMatch(3, 4, MatchStatus.PENDING);

        // when
        match.addParticipant();

        // then
        assertThat(match.getCurrentParticipants()).isEqualTo(4);
        assertThat(match.getStatus()).isEqualTo(MatchStatus.FULL);
    }

    @Test
    @DisplayName("canParticipate - PENDING 상태이고 정원이 남아있으면 true")
    void canParticipate_returnsTrue_whenPendingAndHasCapacity() {
        // given
        Match match = createMatch(2, 4, MatchStatus.PENDING);

        // when
        boolean result = match.canParticipate();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canParticipate - CONFIRMED 상태이고 정원이 남아있으면 true")
    void canParticipate_returnsTrue_whenConfirmedAndHasCapacity() {
        // given
        Match match = createMatch(2, 4, MatchStatus.CONFIRMED);

        // when
        boolean result = match.canParticipate();

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("canParticipate - 정원이 다 찼으면 false")
    void canParticipate_returnsFalse_whenCapacityFull() {
        // given
        Match match = createMatch(4, 4, MatchStatus.PENDING);

        // when
        boolean result = match.canParticipate();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canParticipate - FULL 상태면 false")
    void canParticipate_returnsFalse_whenStatusFull() {
        // given
        Match match = createMatch(4, 4, MatchStatus.FULL);

        // when
        boolean result = match.canParticipate();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("canParticipate - CANCELLED 상태면 false")
    void canParticipate_returnsFalse_whenStatusCancelled() {
        // given
        Match match = createMatch(2, 4, MatchStatus.CANCELLED);

        // when
        boolean result = match.canParticipate();

        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("isHost - 호스트 ID와 같으면 true")
    void isHost_returnsTrue_whenUserIdMatchesHostId() {
        // given
        Match match = createMatchWithHostId(100L);

        // when
        boolean result = match.isHost(100L);

        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("isHost - 호스트 ID와 다르면 false")
    void isHost_returnsFalse_whenUserIdDoesNotMatchHostId() {
        // given
        Match match = createMatchWithHostId(100L);

        // when
        boolean result = match.isHost(999L);

        // then
        assertThat(result).isFalse();
    }

    @Nested
    @DisplayName("removeParticipant 메서드")
    class RemoveParticipantTest {

        @Test
        @DisplayName("참가자 제거 시 currentParticipants가 1 감소한다")
        void removeParticipant_decreasesCurrentParticipants() {
            // given
            Match match = createMatch(3, 4, MatchStatus.PENDING);

            // when
            match.removeParticipant();

            // then
            assertThat(match.getCurrentParticipants()).isEqualTo(2);
        }

        @Test
        @DisplayName("FULL 상태에서 참가자 제거 시 PENDING으로 변경된다")
        void removeParticipant_changesStatusToPending_whenWasFull() {
            // given
            Match match = createMatch(4, 4, MatchStatus.FULL);

            // when
            match.removeParticipant();

            // then
            assertThat(match.getCurrentParticipants()).isEqualTo(3);
            assertThat(match.getStatus()).isEqualTo(MatchStatus.PENDING);
        }

        @Test
        @DisplayName("참가자가 0명일 때 제거해도 음수가 되지 않는다")
        void removeParticipant_doesNotGoBelowZero() {
            // given
            Match match = createMatch(0, 4, MatchStatus.PENDING);

            // when
            match.removeParticipant();

            // then
            assertThat(match.getCurrentParticipants()).isEqualTo(0);
        }

        @Test
        @DisplayName("PENDING 상태에서 참가자 제거 시 상태는 그대로 유지된다")
        void removeParticipant_keepsStatusPending_whenAlreadyPending() {
            // given
            Match match = createMatch(3, 4, MatchStatus.PENDING);

            // when
            match.removeParticipant();

            // then
            assertThat(match.getStatus()).isEqualTo(MatchStatus.PENDING);
        }
    }

    @Nested
    @DisplayName("hasStarted 메서드")
    class HasStartedTest {

        @Test
        @DisplayName("경기 시작 시간이 지났으면 true를 반환한다")
        void hasStarted_returnsTrue_whenMatchTimeHasPassed() {
            // given
            Match match = createMatchWithDate(LocalDate.now().minusDays(1), LocalTime.of(18, 0));

            // when
            boolean result = match.hasStarted();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("경기 시작 시간이 안 되었으면 false를 반환한다")
        void hasStarted_returnsFalse_whenMatchTimeNotYetPassed() {
            // given
            Match match = createMatchWithDate(LocalDate.now().plusDays(1), LocalTime.of(18, 0));

            // when
            boolean result = match.hasStarted();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("오늘 경기인데 시작 시간이 지났으면 true를 반환한다")
        void hasStarted_returnsTrue_whenTodayAndTimeHasPassed() {
            // given
            Match match = createMatchWithDate(LocalDate.now(), LocalTime.now().minusHours(1));

            // when
            boolean result = match.hasStarted();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("오늘 경기인데 시작 시간이 안 되었으면 false를 반환한다")
        void hasStarted_returnsFalse_whenTodayAndTimeNotYetPassed() {
            // given
            Match match = createMatchWithDate(LocalDate.now(), LocalTime.now().plusHours(1));

            // when
            boolean result = match.hasStarted();

            // then
            assertThat(result).isFalse();
        }
    }

    private Match createMatch(int currentParticipants, int maxParticipants, MatchStatus status) {
        return new Match(
                1L,
                0L,  // version
                1L,
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

    private Match createMatchWithHostId(Long hostId) {
        return new Match(
                1L,
                0L,  // version
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
                4,
                1,
                MatchStatus.PENDING
        );
    }

    private Match createMatchWithDate(LocalDate matchDate, LocalTime startTime) {
        return new Match(
                1L,
                0L,  // version
                1L,
                "테스트호스트",
                "테스트 경기",
                "설명",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780),
                "서울시 중구",
                matchDate,
                startTime,
                LocalTime.of(20, 0),
                4,
                2,
                MatchStatus.PENDING
        );
    }
}
