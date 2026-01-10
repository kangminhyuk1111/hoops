package com.hoops.participation.infrastructure.adapter;

import com.hoops.match.application.port.out.MatchParticipationPort;
import com.hoops.participation.application.exception.ParticipationMatchNotFoundException;
import com.hoops.participation.application.port.out.MatchInfo;
import com.hoops.participation.application.port.out.MatchInfoProvider;
import org.springframework.stereotype.Component;

/**
 * Match Context를 통한 경기 정보 제공 어댑터
 *
 * Participation Context의 MatchInfoProvider 포트를 구현하여
 * Match Context가 제공하는 MatchParticipationPort를 통해 경기 정보를 조회합니다.
 *
 * Match Context의 내부 구현(Repository)에 직접 의존하지 않고,
 * Match Context가 외부에 제공하는 Port만 사용합니다.
 */
@Component
public class MatchInfoAdapter implements MatchInfoProvider {

    private final MatchParticipationPort matchParticipationPort;

    public MatchInfoAdapter(MatchParticipationPort matchParticipationPort) {
        this.matchParticipationPort = matchParticipationPort;
    }

    @Override
    public MatchInfo getMatchInfo(Long matchId) {
        MatchParticipationPort.MatchParticipationData data = matchParticipationPort.findMatchForParticipation(matchId)
                .orElseThrow(() -> new ParticipationMatchNotFoundException(matchId));

        return new MatchInfo(
                data.matchId(),
                data.hostId(),
                data.status(),
                data.currentParticipants(),
                data.maxParticipants(),
                data.matchDate(),
                data.startTime()
        );
    }

    @Override
    public void addParticipant(Long matchId) {
        matchParticipationPort.addParticipant(matchId);
    }

    @Override
    public void removeParticipant(Long matchId) {
        matchParticipationPort.removeParticipant(matchId);
    }
}
