package com.hoops.participation.application.port.in;

import com.hoops.participation.domain.Participation;

/**
 * 경기 참가 Use Case
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 경기 참가 비즈니스 로직의 진입점을 정의합니다.
 */
public interface ParticipateInMatchUseCase {

    /**
     * 경기에 참가 신청합니다.
     *
     * @param command 경기 참가 커맨드
     * @return 생성된 참가 정보
     */
    Participation participateInMatch(ParticipateInMatchCommand command);
}
