package com.hoops.participation.application.exception;

import com.hoops.common.exception.ApplicationException;

/**
 * 이미 시작된 경기 예외
 * 이미 시작된 경기의 참가를 취소하려 할 때 발생합니다
 */
public class MatchAlreadyStartedException extends ApplicationException {

    private static final String DEFAULT_ERROR_CODE = "MATCH_ALREADY_STARTED";

    public MatchAlreadyStartedException(Long matchId) {
        super(DEFAULT_ERROR_CODE,
                String.format("이미 시작된 경기는 참가를 취소할 수 없습니다. (matchId: %d)", matchId));
    }
}
