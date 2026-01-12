package com.hoops.match.application.service;

import com.hoops.match.application.exception.MatchAlreadyStartedException;
import com.hoops.match.application.exception.MatchNotFoundException;
import com.hoops.match.application.exception.NotMatchHostException;
import com.hoops.match.application.port.in.CancelMatchCommand;
import com.hoops.match.application.port.in.CancelMatchUseCase;
import com.hoops.match.application.port.out.MatchRepository;
import com.hoops.match.domain.Match;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MatchCanceller implements CancelMatchUseCase {

    private final MatchRepository matchRepository;

    @Override
    public void cancelMatch(CancelMatchCommand command) {
        Match match = matchRepository.findById(command.matchId())
                .orElseThrow(() -> new MatchNotFoundException(command.matchId()));

        if (!match.isHost(command.userId())) {
            throw new NotMatchHostException(command.matchId(), command.userId());
        }

        if (!match.canCancel()) {
            throw new MatchAlreadyStartedException(command.matchId());
        }

        match.cancel();
        matchRepository.save(match);
    }
}
