package com.hoops.match.infrastructure;

import com.hoops.common.domain.BaseTimeEntity;
import com.hoops.match.domain.MatchStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "matches")
public class MatchEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "host_id", nullable = false)
    private Long hostId;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(length = 500)
    private String address;

    @Column(nullable = false)
    private LocalDate matchDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private Integer maxParticipants;

    @Column(nullable = false)
    private Integer currentParticipants;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MatchStatus status;

    protected MatchEntity() {
    }

    public MatchEntity(Long hostId, String title, String description, BigDecimal latitude,
            BigDecimal longitude, String address, LocalDate matchDate, LocalTime startTime,
            LocalTime endTime, Integer maxParticipants) {
        this(hostId, title, description, latitude, longitude, address, matchDate, startTime, endTime,
                maxParticipants, 0, MatchStatus.PENDING);
    }

    public MatchEntity(Long hostId, String title, String description, BigDecimal latitude,
            BigDecimal longitude, String address, LocalDate matchDate, LocalTime startTime,
            LocalTime endTime, Integer maxParticipants, Integer currentParticipants,
            MatchStatus status) {
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
