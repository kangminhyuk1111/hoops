package com.hoops.participation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ParticipationTest {

    @Test
    @DisplayName("Participation 생성 시 모든 필드가 올바르게 설정된다")
    void create_setsAllFieldsCorrectly() {
        // given
        Long id = 1L;
        Long matchId = 100L;
        Long userId = 200L;
        ParticipationStatus status = ParticipationStatus.PENDING;
        LocalDateTime joinedAt = LocalDateTime.of(2026, 1, 10, 14, 30, 0);

        // when
        Participation participation = new Participation(id, matchId, userId, status, joinedAt);

        // then
        assertThat(participation.getId()).isEqualTo(id);
        assertThat(participation.getMatchId()).isEqualTo(matchId);
        assertThat(participation.getUserId()).isEqualTo(userId);
        assertThat(participation.getStatus()).isEqualTo(status);
        assertThat(participation.getJoinedAt()).isEqualTo(joinedAt);
    }

    @Test
    @DisplayName("Participation 생성 시 id가 null이어도 정상 생성된다 (새로 생성하는 경우)")
    void create_withNullId_succeeds() {
        // given
        Long matchId = 100L;
        Long userId = 200L;
        ParticipationStatus status = ParticipationStatus.PENDING;
        LocalDateTime joinedAt = LocalDateTime.now();

        // when
        Participation participation = new Participation(null, matchId, userId, status, joinedAt);

        // then
        assertThat(participation.getId()).isNull();
        assertThat(participation.getMatchId()).isEqualTo(matchId);
        assertThat(participation.getUserId()).isEqualTo(userId);
        assertThat(participation.getStatus()).isEqualTo(status);
    }

    @Test
    @DisplayName("상태 변경 시 새로운 Participation 인스턴스를 생성하여 CONFIRMED 상태로 변경할 수 있다")
    void statusChange_fromPendingToConfirmed_byCreatingNewInstance() {
        // given
        Long id = 1L;
        Long matchId = 100L;
        Long userId = 200L;
        LocalDateTime joinedAt = LocalDateTime.now();

        Participation pendingParticipation = new Participation(
                id, matchId, userId, ParticipationStatus.PENDING, joinedAt);

        // when - 상태 변경을 위해 새 인스턴스 생성 (불변 객체 패턴)
        Participation confirmedParticipation = new Participation(
                pendingParticipation.getId(),
                pendingParticipation.getMatchId(),
                pendingParticipation.getUserId(),
                ParticipationStatus.CONFIRMED,
                pendingParticipation.getJoinedAt()
        );

        // then
        assertThat(pendingParticipation.getStatus()).isEqualTo(ParticipationStatus.PENDING);
        assertThat(confirmedParticipation.getStatus()).isEqualTo(ParticipationStatus.CONFIRMED);
        assertThat(confirmedParticipation.getId()).isEqualTo(pendingParticipation.getId());
        assertThat(confirmedParticipation.getMatchId()).isEqualTo(pendingParticipation.getMatchId());
        assertThat(confirmedParticipation.getUserId()).isEqualTo(pendingParticipation.getUserId());
        assertThat(confirmedParticipation.getJoinedAt()).isEqualTo(pendingParticipation.getJoinedAt());
    }

    @Test
    @DisplayName("CANCELLED 상태로 Participation을 생성할 수 있다")
    void create_withCancelledStatus_succeeds() {
        // given
        Long id = 1L;
        Long matchId = 100L;
        Long userId = 200L;
        LocalDateTime joinedAt = LocalDateTime.now();

        // when
        Participation participation = new Participation(
                id, matchId, userId, ParticipationStatus.CANCELLED, joinedAt);

        // then
        assertThat(participation.getStatus()).isEqualTo(ParticipationStatus.CANCELLED);
    }

    @Test
    @DisplayName("MATCH_CANCELLED 상태로 Participation을 생성할 수 있다")
    void create_withMatchCancelledStatus_succeeds() {
        // given
        Long id = 1L;
        Long matchId = 100L;
        Long userId = 200L;
        LocalDateTime joinedAt = LocalDateTime.now();

        // when
        Participation participation = new Participation(
                id, matchId, userId, ParticipationStatus.MATCH_CANCELLED, joinedAt);

        // then
        assertThat(participation.getStatus()).isEqualTo(ParticipationStatus.MATCH_CANCELLED);
    }

    @Nested
    @DisplayName("cancel 메서드")
    class CancelTest {

        @Test
        @DisplayName("cancel() 호출 시 CANCELLED 상태의 새 인스턴스를 반환한다")
        void cancel_returnsCancelledParticipation() {
            // given
            Participation participation = new Participation(
                    1L, 100L, 200L, ParticipationStatus.CONFIRMED, LocalDateTime.now());

            // when
            Participation cancelled = participation.cancel();

            // then
            assertThat(cancelled.getStatus()).isEqualTo(ParticipationStatus.CANCELLED);
            assertThat(cancelled.getId()).isEqualTo(participation.getId());
            assertThat(cancelled.getMatchId()).isEqualTo(participation.getMatchId());
            assertThat(cancelled.getUserId()).isEqualTo(participation.getUserId());
            assertThat(cancelled.getJoinedAt()).isEqualTo(participation.getJoinedAt());
        }

        @Test
        @DisplayName("cancel() 호출 시 원본 인스턴스는 변경되지 않는다")
        void cancel_doesNotModifyOriginal() {
            // given
            Participation participation = new Participation(
                    1L, 100L, 200L, ParticipationStatus.CONFIRMED, LocalDateTime.now());

            // when
            participation.cancel();

            // then
            assertThat(participation.getStatus()).isEqualTo(ParticipationStatus.CONFIRMED);
        }
    }

    @Nested
    @DisplayName("canCancel 메서드")
    class CanCancelTest {

        @Test
        @DisplayName("CONFIRMED 상태면 true를 반환한다")
        void canCancel_returnsTrue_whenConfirmed() {
            // given
            Participation participation = new Participation(
                    1L, 100L, 200L, ParticipationStatus.CONFIRMED, LocalDateTime.now());

            // when
            boolean result = participation.canCancel();

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("PENDING 상태면 false를 반환한다")
        void canCancel_returnsFalse_whenPending() {
            // given
            Participation participation = new Participation(
                    1L, 100L, 200L, ParticipationStatus.PENDING, LocalDateTime.now());

            // when
            boolean result = participation.canCancel();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("CANCELLED 상태면 false를 반환한다")
        void canCancel_returnsFalse_whenCancelled() {
            // given
            Participation participation = new Participation(
                    1L, 100L, 200L, ParticipationStatus.CANCELLED, LocalDateTime.now());

            // when
            boolean result = participation.canCancel();

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("MATCH_CANCELLED 상태면 false를 반환한다")
        void canCancel_returnsFalse_whenMatchCancelled() {
            // given
            Participation participation = new Participation(
                    1L, 100L, 200L, ParticipationStatus.MATCH_CANCELLED, LocalDateTime.now());

            // when
            boolean result = participation.canCancel();

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isOwner 메서드")
    class IsOwnerTest {

        @Test
        @DisplayName("userId가 일치하면 true를 반환한다")
        void isOwner_returnsTrue_whenUserIdMatches() {
            // given
            Long userId = 200L;
            Participation participation = new Participation(
                    1L, 100L, userId, ParticipationStatus.CONFIRMED, LocalDateTime.now());

            // when
            boolean result = participation.isOwner(userId);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("userId가 다르면 false를 반환한다")
        void isOwner_returnsFalse_whenUserIdDoesNotMatch() {
            // given
            Long userId = 200L;
            Long differentUserId = 999L;
            Participation participation = new Participation(
                    1L, 100L, userId, ParticipationStatus.CONFIRMED, LocalDateTime.now());

            // when
            boolean result = participation.isOwner(differentUserId);

            // then
            assertThat(result).isFalse();
        }
    }
}
