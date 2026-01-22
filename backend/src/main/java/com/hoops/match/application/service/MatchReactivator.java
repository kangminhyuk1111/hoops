package com.hoops.match.application.service;

import com.hoops.match.application.exception.MatchNotFoundException;
import com.hoops.match.domain.exception.MatchCannotReactivateException;
import com.hoops.match.domain.exception.NotMatchHostException;
import com.hoops.match.application.port.in.ReactivateMatchCommand;
import com.hoops.match.application.port.in.ReactivateMatchUseCase;
import com.hoops.match.domain.repository.MatchRepository;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MatchReactivator implements ReactivateMatchUseCase {

    private final MatchRepository matchRepository;

    @Override
    public void reactivateMatch(ReactivateMatchCommand command) {
        Match match = matchRepository.findById(command.matchId())
                .orElseThrow(() -> new MatchNotFoundException(command.matchId()));

        if (!match.isHost(command.userId())) {
            throw new NotMatchHostException(command.matchId(), command.userId());
        }

        if (match.getStatus() != MatchStatus.CANCELLED) {
            throw new MatchCannotReactivateException(command.matchId(), "취소된 경기만 복구할 수 있습니다");
        }

        if (!match.canReactivate()) {
            throw new MatchCannotReactivateException(command.matchId(),
                    "복구 가능 시간(1시간)이 지났거나 경기 날짜가 이미 지났습니다");
        }

        match.reactivate();
        matchRepository.save(match);
    }
}
