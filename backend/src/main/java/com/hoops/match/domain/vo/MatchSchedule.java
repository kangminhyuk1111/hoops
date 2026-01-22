package com.hoops.match.domain.vo;

import com.hoops.match.domain.exception.InvalidTimeRangeException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

public record MatchSchedule(LocalDate date, LocalTime startTime, LocalTime endTime) {

    public MatchSchedule {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(startTime, "startTime must not be null");
        Objects.requireNonNull(endTime, "endTime must not be null");

        if (!startTime.isBefore(endTime)) {
            throw new InvalidTimeRangeException(startTime, endTime);
        }
    }

    public LocalDateTime startDateTime() {
        return LocalDateTime.of(date, startTime);
    }

    public LocalDateTime endDateTime() {
        return LocalDateTime.of(date, endTime);
    }

    public boolean overlapsWith(MatchSchedule other) {
        return this.startDateTime().isBefore(other.endDateTime())
                && other.startDateTime().isBefore(this.endDateTime());
    }

    public boolean overlapsWith(LocalDateTime otherStart, LocalDateTime otherEnd) {
        return this.startDateTime().isBefore(otherEnd)
                && otherStart.isBefore(this.endDateTime());
    }
}
