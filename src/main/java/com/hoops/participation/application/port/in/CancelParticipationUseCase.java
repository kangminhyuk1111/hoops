package com.hoops.participation.application.port.in;

/**
 * 경기 참가 취소 Use Case
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 경기 참가 취소 비즈니스 로직의 진입점을 정의합니다.
 */
public interface CancelParticipationUseCase {

    /**
     * 경기 참가를 취소합니다.
     *
     * @param command 참가 취소 커맨드
     */
    void cancelParticipation(CancelParticipationCommand command);
}
