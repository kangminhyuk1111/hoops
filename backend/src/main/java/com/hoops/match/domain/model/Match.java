package com.hoops.match.domain.model;

import com.hoops.match.domain.exception.CancelTimeExceededException;
import com.hoops.match.domain.exception.InvalidMaxParticipantsUpdateException;
import com.hoops.match.domain.exception.MatchCannotBeCancelledException;
import com.hoops.match.domain.exception.MatchCannotBeUpdatedException;
import com.hoops.match.domain.exception.MatchCannotReactivateException;
import com.hoops.match.domain.exception.NotMatchHostException;
import com.hoops.match.domain.vo.MatchStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
public class Match {

    private static final int REACTIVATE_TIME_LIMIT_HOURS = 1;
    private static final int CANCEL_DEADLINE_HOURS = 2;
    private static final int INITIAL_PARTICIPANTS = 1;

    private final Long id;
    private final Long version;
    private final Long hostId;
    private final String hostNickname;
    private final String title;
    private final String description;
    private final BigDecimal latitude;
    private final BigDecimal longitude;
    private final String address;
    private final LocalDate matchDate;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Integer maxParticipants;
    private Integer currentParticipants;
    private MatchStatus status;
    private LocalDateTime cancelledAt;

    // Private constructor - use factory methods
    private Match(Long id, Long version, Long hostId, String hostNickname,
                  String title, String description, BigDecimal latitude, BigDecimal longitude,
                  String address, LocalDate matchDate, LocalTime startTime, LocalTime endTime,
                  Integer maxParticipants, Integer currentParticipants, MatchStatus status,
                  LocalDateTime cancelledAt) {
        this.id = id;
        this.version = version;
        this.hostId = hostId;
        this.hostNickname = hostNickname;
        this.title = title;
        this.description = description;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.matchDate = matchDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxParticipants = maxParticipants;
        this.currentParticipants = currentParticipants;
        this.status = status;
        this.cancelledAt = cancelledAt;
    }

    // ==================== Factory Methods ====================

    /**
     * Create a new Match (no id yet)
     */
    public static Match create(Long hostId, String hostNickname, String title, String description,
                               BigDecimal latitude, BigDecimal longitude, String address,
                               LocalDate matchDate, LocalTime startTime, LocalTime endTime,
                               Integer maxParticipants) {
        return new Match(
                null,
                null,
                hostId,
                hostNickname,
                title,
                description,
                latitude,
                longitude,
                address,
                matchDate,
                startTime,
                endTime,
                maxParticipants,
                INITIAL_PARTICIPANTS,
                MatchStatus.PENDING,
                null
        );
    }

    /**
     * Reconstitute a Match from persistence (has id)
     */
    public static Match reconstitute(Long id, Long version, Long hostId, String hostNickname,
                                     String title, String description, BigDecimal latitude,
                                     BigDecimal longitude, String address, LocalDate matchDate,
                                     LocalTime startTime, LocalTime endTime, Integer maxParticipants,
                                     Integer currentParticipants, MatchStatus status,
                                     LocalDateTime cancelledAt) {
        return new Match(id, version, hostId, hostNickname, title, description,
                latitude, longitude, address, matchDate, startTime, endTime,
                maxParticipants, currentParticipants, status, cancelledAt);
    }

    // ==================== Domain Behaviors with Self-Validation ====================

    /**
     * Reactivate a cancelled match (validates host and reactivation rules)
     */
    public void reactivate(Long requestUserId) {
        validateHost(requestUserId);
        validateReactivatable();
        this.status = MatchStatus.PENDING;
        this.cancelledAt = null;
    }

    /**
     * Cancel the match (validates host and cancellation rules)
     */
    public void cancel(Long requestUserId) {
        validateHost(requestUserId);
        validateCancellable();
        this.status = MatchStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * Validate and update the match (returns new Match instance)
     */
    public Match updateWith(Long requestUserId, String newTitle, String newDescription,
                            LocalDate newMatchDate, LocalTime newStartTime, LocalTime newEndTime,
                            Integer newMaxParticipants) {
        validateHost(requestUserId);
        validateUpdatable();
        if (newMaxParticipants != null) {
            validateMaxParticipantsUpdate(newMaxParticipants);
        }

        MatchStatus newStatus = calculateNewStatus(newMaxParticipants);

        return new Match(
                this.id,
                this.version,
                this.hostId,
                this.hostNickname,
                newTitle != null ? newTitle : this.title,
                newDescription != null ? newDescription : this.description,
                this.latitude,
                this.longitude,
                this.address,
                newMatchDate != null ? newMatchDate : this.matchDate,
                newStartTime != null ? newStartTime : this.startTime,
                newEndTime != null ? newEndTime : this.endTime,
                newMaxParticipants != null ? newMaxParticipants : this.maxParticipants,
                this.currentParticipants,
                newStatus,
                this.cancelledAt
        );
    }

    // ==================== Validation Methods ====================

    private void validateHost(Long userId) {
        if (!this.hostId.equals(userId)) {
            throw new NotMatchHostException(this.id, userId);
        }
    }

    private void validateReactivatable() {
        if (this.status != MatchStatus.CANCELLED) {
            throw new MatchCannotReactivateException(this.id, "Only cancelled matches can be reactivated");
        }
        if (this.cancelledAt == null) {
            throw new MatchCannotReactivateException(this.id, "Cancellation time not recorded");
        }
        if (this.matchDate.isBefore(LocalDate.now())) {
            throw new MatchCannotReactivateException(this.id, "Match date has already passed");
        }
        LocalDateTime reactivateDeadline = this.cancelledAt.plusHours(REACTIVATE_TIME_LIMIT_HOURS);
        if (LocalDateTime.now().isAfter(reactivateDeadline)) {
            throw new MatchCannotReactivateException(this.id, "Reactivation time limit (1 hour) exceeded");
        }
    }

    private void validateCancellable() {
        if (!canCancel()) {
            throw new MatchCannotBeCancelledException(this.id, "Match is already started, ended, or cancelled");
        }
        if (!canCancelByTime()) {
            throw new CancelTimeExceededException(this.id);
        }
    }

    private void validateUpdatable() {
        if (!canUpdate()) {
            throw new MatchCannotBeUpdatedException(this.id, this.status.name());
        }
    }

    private void validateMaxParticipantsUpdate(Integer newMaxParticipants) {
        if (newMaxParticipants < this.currentParticipants) {
            throw new InvalidMaxParticipantsUpdateException(this.id, this.currentParticipants, newMaxParticipants);
        }
    }

    private MatchStatus calculateNewStatus(Integer newMaxParticipants) {
        if (newMaxParticipants != null && this.currentParticipants >= newMaxParticipants) {
            return MatchStatus.FULL;
        }
        if (this.status == MatchStatus.FULL && newMaxParticipants != null
                && this.currentParticipants < newMaxParticipants) {
            return MatchStatus.PENDING;
        }
        return this.status;
    }

    // ==================== Participant Management ====================

    public void addParticipant() {
        this.currentParticipants++;
        if (this.currentParticipants >= this.maxParticipants) {
            this.status = MatchStatus.FULL;
        }
    }

    public void removeParticipant() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
        }
        if (this.status == MatchStatus.FULL) {
            this.status = MatchStatus.PENDING;
        }
    }

    // ==================== Status Transitions ====================

    public void startMatch() {
        if (canTransitionToInProgress()) {
            this.status = MatchStatus.IN_PROGRESS;
        }
    }

    public void endMatch() {
        if (canTransitionToEnded()) {
            this.status = MatchStatus.ENDED;
        }
    }

    // ==================== Query Methods ====================

    public boolean isHost(Long userId) {
        return this.hostId.equals(userId);
    }

    public boolean hasStarted() {
        LocalDateTime matchStartDateTime = LocalDateTime.of(this.matchDate, this.startTime);
        return LocalDateTime.now().isAfter(matchStartDateTime);
    }

    public boolean hasEnded() {
        LocalDateTime matchEndDateTime = LocalDateTime.of(this.matchDate, this.endTime);
        return LocalDateTime.now().isAfter(matchEndDateTime);
    }

    public boolean shouldStartNow() {
        return hasStarted() && !hasEnded() && canTransitionToInProgress();
    }

    public boolean shouldEndNow() {
        return hasEnded() && canTransitionToEnded();
    }

    public boolean canTransitionToInProgress() {
        return this.status == MatchStatus.PENDING
                || this.status == MatchStatus.CONFIRMED
                || this.status == MatchStatus.FULL;
    }

    public boolean canTransitionToEnded() {
        return this.status == MatchStatus.IN_PROGRESS;
    }

    public boolean canParticipate() {
        return (this.status == MatchStatus.PENDING || this.status == MatchStatus.CONFIRMED)
                && this.currentParticipants < this.maxParticipants;
    }

    public boolean isFull() {
        return this.currentParticipants >= this.maxParticipants;
    }

    public boolean canCancel() {
        return this.status != MatchStatus.IN_PROGRESS
                && this.status != MatchStatus.ENDED
                && this.status != MatchStatus.CANCELLED;
    }

    public boolean canCancelByTime() {
        LocalDateTime matchStartDateTime = LocalDateTime.of(this.matchDate, this.startTime);
        LocalDateTime cancelDeadline = matchStartDateTime.minusHours(CANCEL_DEADLINE_HOURS);
        return LocalDateTime.now().isBefore(cancelDeadline);
    }

    public boolean canUpdate() {
        return this.status == MatchStatus.PENDING
                || this.status == MatchStatus.CONFIRMED;
    }

    public boolean canReactivate() {
        if (this.status != MatchStatus.CANCELLED) {
            return false;
        }
        if (this.cancelledAt == null) {
            return false;
        }
        if (this.matchDate.isBefore(LocalDate.now())) {
            return false;
        }
        LocalDateTime reactivateDeadline = this.cancelledAt.plusHours(REACTIVATE_TIME_LIMIT_HOURS);
        return LocalDateTime.now().isBefore(reactivateDeadline);
    }

    public LocalDateTime getStartDateTime() {
        return LocalDateTime.of(this.matchDate, this.startTime);
    }

    public LocalDateTime getEndDateTime() {
        return LocalDateTime.of(this.matchDate, this.endTime);
    }

    public boolean overlapsWithTime(LocalDateTime otherStart, LocalDateTime otherEnd) {
        LocalDateTime thisStart = getStartDateTime();
        LocalDateTime thisEnd = getEndDateTime();
        return thisStart.isBefore(otherEnd) && otherStart.isBefore(thisEnd);
    }
}
