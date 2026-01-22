package com.hoops.match.domain.model;

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

    public void cancel() {
        this.status = MatchStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public void reactivate() {
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

    public Match update(String title, String description, LocalDate matchDate,
                        LocalTime startTime, LocalTime endTime, Integer maxParticipants) {
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

    public boolean overlapsWithTime(LocalDateTime otherStart, LocalDateTime otherEnd) {
        LocalDateTime thisStart = getStartDateTime();
        LocalDateTime thisEnd = getEndDateTime();
        return thisStart.isBefore(otherEnd) && otherStart.isBefore(thisEnd);
    }
}
