package com.hoops.participation.application.port.out;

import com.hoops.common.event.ParticipationEvent;

/**
 * 참가 이벤트 발행을 위한 아웃바운드 포트
 */
public interface ParticipationEventPublisher {

    void publish(ParticipationEvent event);
}
