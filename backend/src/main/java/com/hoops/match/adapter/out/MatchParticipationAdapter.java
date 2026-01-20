package com.hoops.match.adapter.out;

import com.hoops.match.application.exception.MatchFullException;
import com.hoops.match.application.exception.MatchNotFoundException;
import com.hoops.match.application.port.out.MatchParticipationPort;
import com.hoops.match.domain.repository.MatchRepository;
import com.hoops.match.domain.model.Match;
import java.util.List;
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
                .map(this::toMatchParticipationData);
    }

    @Override
    public List<MatchParticipationData> findMatchesForParticipation(List<Long> matchIds) {
        return matchRepository.findAllByIds(matchIds).stream()
                .map(this::toMatchParticipationData)
                .toList();
    }

    private MatchParticipationData toMatchParticipationData(Match match) {
        return new MatchParticipationData(
                match.getId(),
                match.getHostId(),
                match.getTitle(),
                match.getStatus().name(),
                match.getCurrentParticipants(),
                match.getMaxParticipants(),
                match.getMatchDate(),
                match.getStartTime(),
                match.getEndTime()
        );
    }

    @Override
    @Transactional(timeout = 5)
    public void addParticipant(Long matchId) {
        Match match = matchRepository.findByIdWithLock(matchId)
                .orElseThrow(() -> new MatchNotFoundException(matchId));

        if (match.isFull()) {
            throw new MatchFullException(matchId);
        }

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
