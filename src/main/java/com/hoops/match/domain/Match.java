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

    public boolean canParticipate() {
        return (this.status == MatchStatus.PENDING || this.status == MatchStatus.CONFIRMED)
                && this.currentParticipants < this.maxParticipants;
    }

    public boolean isHost(Long userId) {
        return this.hostId.equals(userId);
    }

    public void cancel() {
        this.status = MatchStatus.CANCELLED;
    }

    public boolean canCancel() {
        return this.status != MatchStatus.IN_PROGRESS
                && this.status != MatchStatus.ENDED
                && this.status != MatchStatus.CANCELLED;
    }
}
