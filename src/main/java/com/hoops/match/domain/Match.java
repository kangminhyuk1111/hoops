package com.hoops.match.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Match {

    private Long id;
    private Long version;
    private Long hostId;
    private String hostNickname;
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

    public Match(Long id, Long version, Long hostId, String hostNickname, String title, String description,
            BigDecimal latitude, BigDecimal longitude, String address, LocalDate matchDate,
            LocalTime startTime, LocalTime endTime, Integer maxParticipants,
            Integer currentParticipants, MatchStatus status) {
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
    }

    // Domain Logic

    /**
     * 참가자를 추가합니다.
     * 참가자 수를 증가시키고, 정원이 다 차면 상태를 FULL로 변경합니다.
     */
    public void addParticipant() {
        this.currentParticipants++;

        if (this.currentParticipants >= this.maxParticipants) {
            this.status = MatchStatus.FULL;
        }
    }

    /**
     * 참가자를 제거합니다.
     * 참가자 수를 감소시키고, 상태가 FULL이었다면 PENDING으로 변경합니다.
     */
    public void removeParticipant() {
        if (this.currentParticipants > 0) {
            this.currentParticipants--;
        }

        if (this.status == MatchStatus.FULL) {
            this.status = MatchStatus.PENDING;
        }
    }

    /**
     * 경기가 이미 시작되었는지 확인합니다.
     *
     * @return 경기가 시작되었으면 true, 아니면 false
     */
    public boolean hasStarted() {
        LocalDateTime matchStartDateTime = LocalDateTime.of(this.matchDate, this.startTime);
        return LocalDateTime.now().isAfter(matchStartDateTime);
    }

    /**
     * 참가 가능한 경기인지 확인합니다.
     *
     * @return 참가 가능하면 true, 아니면 false
     */
    public boolean canParticipate() {
        return (this.status == MatchStatus.PENDING || this.status == MatchStatus.CONFIRMED)
                && this.currentParticipants < this.maxParticipants;
    }

    /**
     * 사용자가 경기의 호스트인지 확인합니다.
     *
     * @param userId 확인할 사용자 ID
     * @return 호스트이면 true, 아니면 false
     */
    public boolean isHost(Long userId) {
        return this.hostId.equals(userId);
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
    }

    public Long getHostId() {
        return hostId;
    }

    public String getHostNickname() {
        return hostNickname;
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
