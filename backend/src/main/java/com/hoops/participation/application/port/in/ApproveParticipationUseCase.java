package com.hoops.participation.application.port.in;

import com.hoops.participation.domain.model.Participation;

/**
 * 경기 참가 승인 Use Case
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 경기 호스트가 참가 신청을 승인하는 비즈니스 로직의 진입점을 정의합니다.
 */
public interface ApproveParticipationUseCase {

    /**
     * 경기 참가 신청을 승인합니다.
     *
     * @param command 참가 승인 커맨드
     * @return 승인된 참가 정보
     */
    Participation approveParticipation(ApproveParticipationCommand command);
}
