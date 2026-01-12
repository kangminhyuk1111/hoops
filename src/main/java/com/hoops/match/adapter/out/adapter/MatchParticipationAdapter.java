package com.hoops.match.adapter.out.adapter;

import com.hoops.match.application.exception.MatchNotFoundException;
import com.hoops.match.application.port.out.MatchParticipationPort;
import com.hoops.match.application.port.out.MatchRepository;
import com.hoops.match.domain.Match;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Match Context 내부 어댑터
 *
 * MatchParticipationPort를 구현하여 외부 컨텍스트(Participation 등)에
 * 경기 참가 관련 기능을 제공합니다.
 */
@Component
@RequiredArgsConstructor
public class MatchParticipationAdapter implements MatchParticipationPort {

    private final MatchRepository matchRepository;

    @Override
    public Optional<MatchParticipationData> findMatchForParticipation(Long matchId) {
        return matchRepository.findById(matchId)
                .map(match -> new MatchParticipationData(
                        match.getId(),
                        match.getHostId(),
                        match.getStatus().name(),
                        match.getCurrentParticipants(),
                        match.getMaxParticipants(),
                        match.getMatchDate(),
                        match.getStartTime()
                ));
    }

    @Override
    @Transactional
    public void addParticipant(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        match.addParticipant();
        matchRepository.save(match);
    }

    @Override
    @Transactional
    public void removeParticipant(Long matchId) {
        Match match = matchRepository.findById(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        match.removeParticipant();
        matchRepository.save(match);
    }
}
