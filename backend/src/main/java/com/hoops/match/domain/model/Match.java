package com.hoops.match.domain.model;

import com.hoops.match.domain.exception.CancelTimeExceededException;
import com.hoops.match.domain.exception.InvalidMaxParticipantsUpdateException;
import com.hoops.match.domain.exception.MatchAlreadyStartedException;
import com.hoops.match.domain.exception.MatchCannotBeUpdatedException;
import com.hoops.match.domain.exception.MatchCannotReactivateException;
import com.hoops.match.domain.exception.NotMatchHostException;
import com.hoops.match.domain.vo.MatchHost;
import com.hoops.match.domain.vo.MatchLocation;
import com.hoops.match.domain.vo.MatchSchedule;
import com.hoops.match.domain.vo.MatchStatus;
import com.hoops.match.domain.vo.RecruitmentStatus;
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
     * Create a new Match with Value Objects.
     * Validates domain invariants (time range is validated in MatchSchedule).
     */
    public static Match create(MatchHost host, String title, String description,
                               MatchLocation location, MatchSchedule schedule,
                               Integer maxParticipants) {
        return new Match(
                null,
                null,
                host.id(),
                host.nickname(),
                title,
                description,
                location.latitude(),
                location.longitude(),
                location.address(),
                schedule.date(),
                schedule.startTime(),
                schedule.endTime(),
                maxParticipants,
                INITIAL_PARTICIPANTS,
                MatchStatus.PENDING,
                null
        );
    }

    /**
     * Reconstitute a Match from persistence.
     * Skips validation as data is assumed valid.
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

    // ==================== Domain Behaviors ====================

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

    /**
     * Cancel this match. Validates host and cancellation rules internally.
     */
    public void cancel(Long requestUserId) {
        validateHost(requestUserId);
        validateCanCancel();
        validateCancelDeadline();

        this.status = MatchStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    /**
     * Reactivate a cancelled match. Validates host and reactivation rules internally.
     */
    public void reactivate(Long requestUserId) {
        validateHost(requestUserId);
        validateCanReactivate();

        this.status = MatchStatus.PENDING;
        this.cancelledAt = null;
    }

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

    /**
     * Update this match. Validates host and update rules internally.
     */
    public Match update(Long requestUserId, String title, String description,
                        LocalDate matchDate, LocalTime startTime, LocalTime endTime,
                        Integer maxParticipants) {
        validateHost(requestUserId);
        validateCanUpdate();
        validateMaxParticipantsUpdate(maxParticipants);

        MatchStatus newStatus = this.status;
        if (maxParticipants != null && this.currentParticipants >= maxParticipants) {
            newStatus = MatchStatus.FULL;
        } else if (this.status == MatchStatus.FULL && maxParticipants != null
                && this.currentParticipants < maxParticipants) {
            newStatus = MatchStatus.PENDING;
        }

        return new Match(
                this.id,
                this.version,
                this.hostId,
                this.hostNickname,
                title != null ? title : this.title,
                description != null ? description : this.description,
                this.latitude,
                this.longitude,
                this.address,
                matchDate != null ? matchDate : this.matchDate,
                startTime != null ? startTime : this.startTime,
                endTime != null ? endTime : this.endTime,
                maxParticipants != null ? maxParticipants : this.maxParticipants,
                this.currentParticipants,
                newStatus,
                this.cancelledAt
        );
    }

    // ==================== Private Validation Methods ====================

    private void validateHost(Long requestUserId) {
        if (!this.hostId.equals(requestUserId)) {
            throw new NotMatchHostException(this.id, requestUserId);
        }
    }

    private void validateCanCancel() {
        if (this.status == MatchStatus.IN_PROGRESS
                || this.status == MatchStatus.ENDED
                || this.status == MatchStatus.CANCELLED) {
            throw new MatchAlreadyStartedException(this.id);
        }
    }

    private void validateCancelDeadline() {
        LocalDateTime matchStartDateTime = LocalDateTime.of(this.matchDate, this.startTime);
        LocalDateTime cancelDeadline = matchStartDateTime.minusHours(CANCEL_DEADLINE_HOURS);
        if (!LocalDateTime.now().isBefore(cancelDeadline)) {
            throw new CancelTimeExceededException(this.id);
        }
    }

    private void validateCanReactivate() {
        if (this.status != MatchStatus.CANCELLED) {
            throw new MatchCannotReactivateException(this.id, "Only cancelled matches can be reactivated");
        }
        if (this.cancelledAt == null) {
            throw new MatchCannotReactivateException(this.id, "Cancelled time is not recorded");
        }
        if (this.matchDate.isBefore(LocalDate.now())) {
            throw new MatchCannotReactivateException(this.id, "Match date has already passed");
        }
        LocalDateTime reactivateDeadline = this.cancelledAt.plusHours(REACTIVATE_TIME_LIMIT_HOURS);
        if (!LocalDateTime.now().isBefore(reactivateDeadline)) {
            throw new MatchCannotReactivateException(this.id, "Reactivation deadline (1 hour) has passed");
        }
    }

    private void validateCanUpdate() {
        if (this.status != MatchStatus.PENDING && this.status != MatchStatus.CONFIRMED) {
            throw new MatchCannotBeUpdatedException(this.id, this.status.name());
        }
    }

    private void validateMaxParticipantsUpdate(Integer newMaxParticipants) {
        if (newMaxParticipants != null && newMaxParticipants < this.currentParticipants) {
            throw new InvalidMaxParticipantsUpdateException(
                    this.id,
                    this.currentParticipants,
                    newMaxParticipants
            );
        }
    }

    // ==================== Query Methods ====================

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

    public boolean isHost(Long userId) {
        return this.hostId.equals(userId);
    }

    public boolean canUpdate() {
        return this.status == MatchStatus.PENDING
                || this.status == MatchStatus.CONFIRMED;
    }

    public LocalDateTime getStartDateTime() {
        return LocalDateTime.of(this.matchDate, this.startTime);
    }

    public LocalDateTime getEndDateTime() {
        return LocalDateTime.of(this.matchDate, this.endTime);
    }

    public MatchSchedule getSchedule() {
        return new MatchSchedule(this.matchDate, this.startTime, this.endTime);
    }

    public boolean overlapsWithTime(LocalDateTime otherStart, LocalDateTime otherEnd) {
        LocalDateTime thisStart = getStartDateTime();
        LocalDateTime thisEnd = getEndDateTime();
        return thisStart.isBefore(otherEnd) && otherStart.isBefore(thisEnd);
    }

    public int getRemainingSlots() {
        return Math.max(0, this.maxParticipants - this.currentParticipants);
    }

    public RecruitmentStatus getRecruitmentStatus() {
        return RecruitmentStatus.from(this.currentParticipants, this.maxParticipants);
    }
}
