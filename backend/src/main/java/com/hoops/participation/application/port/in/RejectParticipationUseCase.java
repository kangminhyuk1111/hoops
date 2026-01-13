package com.hoops.participation.application.port.in;

import com.hoops.participation.domain.Participation;

/**
 * 경기 참가 거절 Use Case
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 경기 호스트가 참가 신청을 거절하는 비즈니스 로직의 진입점을 정의합니다.
 */
public interface RejectParticipationUseCase {

    /**
     * 경기 참가 신청을 거절합니다.
     *
     * @param command 참가 거절 커맨드
     * @return 거절된 참가 정보
     */
    Participation rejectParticipation(RejectParticipationCommand command);
}
