package com.hoops.participation.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MatchInfoTest {

    @Nested
    @DisplayName("isHost 메서드")
    class IsHostTest {

        @Test
        @DisplayName("hostId와 userId가 같으면 true를 반환한다")
        void isHost_returnsTrue_whenUserIdMatchesHostId() {
            // given
            Long hostId = 100L;
            MatchInfo matchInfo = createMatchInfo(hostId, "PENDING", 5, 10);

            // when
            boolean result = matchInfo.isHost(hostId);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("hostId와 userId가 다르면 false를 반환한다")
        void isHost_returnsFalse_whenUserIdDoesNotMatchHostId() {
            // given
            Long hostId = 100L;
            Long differentUserId = 999L;
            MatchInfo matchInfo = createMatchInfo(hostId, "PENDING", 5, 10);

            // when
            boolean result = matchInfo.isHost(differentUserId);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("canParticipate 메서드")
    class CanParticipateTest {

        @Test
        @DisplayName("PENDING 상태이고 정원이 남아있으면 true를 반환한다")
        void canParticipate_returnsTrue_whenPendingAndHasCapacity() {
            // given
            MatchInfo matchInfo = createMatchInfo(1L, "PENDING", 5, 10);

            // when
            boolean result = matchInfo.canParticipate();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("CONFIRMED 상태이고 정원이 남아있으면 true를 반환한다")
        void canParticipate_returnsTrue_whenConfirmedAndHasCapacity() {
            // given
            MatchInfo matchInfo = createMatchInfo(1L, "CONFIRMED", 5, 10);

            // when
            boolean result = matchInfo.canParticipate();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("정원이 다 찼으면 false를 반환한다")
        void canParticipate_returnsFalse_whenCapacityFull() {
            // given
            MatchInfo matchInfo = createMatchInfo(1L, "PENDING", 10, 10);

            // when
            boolean result = matchInfo.canParticipate();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("FULL 상태면 false를 반환한다")
        void canParticipate_returnsFalse_whenStatusFull() {
            // given
            MatchInfo matchInfo = createMatchInfo(1L, "FULL", 10, 10);

            // when
            boolean result = matchInfo.canParticipate();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("CANCELLED 상태면 false를 반환한다")
        void canParticipate_returnsFalse_whenStatusCancelled() {
            // given
            MatchInfo matchInfo = createMatchInfo(1L, "CANCELLED", 5, 10);

            // when
            boolean result = matchInfo.canParticipate();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("COMPLETED 상태면 false를 반환한다")
        void canParticipate_returnsFalse_whenStatusCompleted() {
            // given
            MatchInfo matchInfo = createMatchInfo(1L, "COMPLETED", 5, 10);

            // when
            boolean result = matchInfo.canParticipate();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("참가자가 0명이어도 정원이 남아있고 PENDING이면 true를 반환한다")
        void canParticipate_returnsTrue_whenNoParticipantsYet() {
            // given
            MatchInfo matchInfo = createMatchInfo(1L, "PENDING", 0, 10);

            // when
            boolean result = matchInfo.canParticipate();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("정원이 1명 초과되어도 false를 반환한다")
        void canParticipate_returnsFalse_whenOverCapacity() {
            // given
            MatchInfo matchInfo = createMatchInfo(1L, "PENDING", 11, 10);

            // when
            boolean result = matchInfo.canParticipate();

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("hasStarted 메서드")
    class HasStartedTest {

        @Test
        @DisplayName("경기 시작 시간이 지났으면 true를 반환한다")
        void hasStarted_returnsTrue_whenMatchTimeHasPassed() {
            // given
            MatchInfo matchInfo = new MatchInfo(1L, 1L, "PENDING", 5, 10,
                    LocalDate.now().minusDays(1), LocalTime.of(18, 0));

            // when
            boolean result = matchInfo.hasStarted();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("경기 시작 시간이 아직 안 되었으면 false를 반환한다")
        void hasStarted_returnsFalse_whenMatchTimeNotYetPassed() {
            // given
            MatchInfo matchInfo = new MatchInfo(1L, 1L, "PENDING", 5, 10,
                    LocalDate.now().plusDays(1), LocalTime.of(18, 0));

            // when
            boolean result = matchInfo.hasStarted();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("오늘 경기인데 시작 시간이 지났으면 true를 반환한다")
        void hasStarted_returnsTrue_whenTodayAndTimeHasPassed() {
            // given - 현재 시간보다 1시간 전
            MatchInfo matchInfo = new MatchInfo(1L, 1L, "PENDING", 5, 10,
                    LocalDate.now(), LocalTime.now().minusHours(1));

            // when
            boolean result = matchInfo.hasStarted();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("오늘 경기인데 시작 시간이 안 되었으면 false를 반환한다")
        void hasStarted_returnsFalse_whenTodayAndTimeNotYetPassed() {
            // given - 현재 시간보다 1시간 후
            MatchInfo matchInfo = new MatchInfo(1L, 1L, "PENDING", 5, 10,
                    LocalDate.now(), LocalTime.now().plusHours(1));

            // when
            boolean result = matchInfo.hasStarted();

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isFull 메서드")
    class IsFullTest {

        @Test
        @DisplayName("currentParticipants가 maxParticipants와 같으면 true를 반환한다")
        void isFull_returnsTrue_whenCurrentEqualsMax() {
            // given
            MatchInfo matchInfo = createMatchInfo(1L, "PENDING", 10, 10);

            // when
            boolean result = matchInfo.isFull();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("currentParticipants가 maxParticipants보다 크면 true를 반환한다")
        void isFull_returnsTrue_whenCurrentExceedsMax() {
            // given
            MatchInfo matchInfo = createMatchInfo(1L, "PENDING", 11, 10);

            // when
            boolean result = matchInfo.isFull();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("currentParticipants가 maxParticipants보다 작으면 false를 반환한다")
        void isFull_returnsFalse_whenCurrentLessThanMax() {
            // given
            MatchInfo matchInfo = createMatchInfo(1L, "PENDING", 5, 10);

            // when
            boolean result = matchInfo.isFull();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("참가자가 0명이면 false를 반환한다")
        void isFull_returnsFalse_whenNoParticipants() {
            // given
            MatchInfo matchInfo = createMatchInfo(1L, "PENDING", 0, 10);

            // when
            boolean result = matchInfo.isFull();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("1명 자리가 남았으면 false를 반환한다")
        void isFull_returnsFalse_whenOneSlotRemaining() {
            // given
            MatchInfo matchInfo = createMatchInfo(1L, "PENDING", 9, 10);

            // when
            boolean result = matchInfo.isFull();

            // then
            assertThat(result).isFalse();
        }
    }

    private MatchInfo createMatchInfo(Long hostId, String status,
            Integer currentParticipants, Integer maxParticipants) {
        return new MatchInfo(1L, hostId, status, currentParticipants, maxParticipants,
                LocalDate.now().plusDays(1), LocalTime.of(18, 0));
    }
}
