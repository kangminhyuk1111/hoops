package com.hoops.match.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public class Match {

    private Long id;
    private Long hostId;
    private String title;
    private String description;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String address;
    private LocalDate matchDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer maxParticipants;
    private Integer currentParticipants;
    private MatchStatus status;

    public Match(Long id, Long hostId, String title, String description, BigDecimal latitude,
            BigDecimal longitude, String address, LocalDate matchDate, LocalTime startTime,
            LocalTime endTime, Integer maxParticipants, Integer currentParticipants,
            MatchStatus status) {
        this.id = id;
        this.hostId = hostId;
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
    }

    // Domain Logic - None strictly required yet as per YAGNI

    // Getters
    public Long getId() {
        return id;
    }

    public Long getHostId() {
        return hostId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public LocalDate getMatchDate() {
        return matchDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public Integer getMaxParticipants() {
        return maxParticipants;
    }

    public Integer getCurrentParticipants() {
        return currentParticipants;
    }

    public MatchStatus getStatus() {
        return status;
    }
}
