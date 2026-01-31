package com.hoops.match.domain.vo;

public enum RecruitmentStatus {
    RECRUITING, ALMOST_FULL, FULL;

    private static final int ALMOST_FULL_THRESHOLD = 2;

    public static RecruitmentStatus from(int currentParticipants, int maxParticipants) {
        int remaining = maxParticipants - currentParticipants;
        if (remaining <= 0) return FULL;
        if (remaining <= ALMOST_FULL_THRESHOLD) return ALMOST_FULL;
        return RECRUITING;
    }
}
