package com.hoops.match.domain.vo;

import com.hoops.match.domain.exception.InvalidSortTypeException;

public enum MatchSortType {
    DISTANCE, URGENCY;

    public static MatchSortType from(String value) {
        for (MatchSortType type : values()) {
            if (type.name().equalsIgnoreCase(value)) return type;
        }
        throw new InvalidSortTypeException(value);
    }
}
