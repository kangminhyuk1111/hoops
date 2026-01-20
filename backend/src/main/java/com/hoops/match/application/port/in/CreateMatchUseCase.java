package com.hoops.match.application.port.in;

import com.hoops.match.domain.model.Match;

/**
 * 경기 생성 Use Case
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 경기 생성 비즈니스 로직의 진입점을 정의합니다.
 */
public interface CreateMatchUseCase {

    /**
     * 새로운 경기를 생성합니다.
     *
     * @param command 경기 생성 커맨드
     * @return 생성된 경기
     */
    Match createMatch(CreateMatchCommand command);
}
