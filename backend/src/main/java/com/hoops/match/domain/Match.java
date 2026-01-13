package com.hoops.match.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Builder
@AllArgsConstructor
public class Match {

    private static final int REACTIVATE_TIME_LIMIT_HOURS = 1;

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

    public boolean canParticipate() {
        return (this.status == MatchStatus.PENDING || this.status == MatchStatus.CONFIRMED)
                && this.currentParticipants < this.maxParticipants;
    }

    public boolean isHost(Long userId) {
        return this.hostId.equals(userId);
    }

    public void cancel() {
        this.status = MatchStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
    }

    public boolean canCancel() {
        return this.status != MatchStatus.IN_PROGRESS
                && this.status != MatchStatus.ENDED
                && this.status != MatchStatus.CANCELLED;
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

    public void reactivate() {
        this.status = MatchStatus.PENDING;
        this.cancelledAt = null;
    }

    public boolean canUpdate() {
        return this.status == MatchStatus.PENDING
                || this.status == MatchStatus.CONFIRMED;
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
}
