package com.hoops.location.application.port.in;

import com.hoops.location.domain.model.Location;

/**
 * 장소 생성 Use Case
 *
 * Hexagonal Architecture의 Inbound Port로서,
 * 장소 생성 비즈니스 로직의 진입점을 정의합니다.
 */
public interface CreateLocationUseCase {

    /**
     * 새로운 장소를 생성합니다.
     *
     * @param command 장소 생성 커맨드
     * @return 생성된 장소
     */
    Location createLocation(CreateLocationCommand command);
}
