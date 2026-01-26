package com.hoops.participation.application.service;

import com.hoops.common.exception.BusinessException;
import com.hoops.participation.application.exception.ParticipationConflictException;
import com.hoops.participation.application.port.in.ParticipateInMatchCommand;
import com.hoops.participation.application.port.in.ParticipateInMatchUseCase;
import com.hoops.participation.application.port.out.MatchInfo;
import com.hoops.participation.application.port.out.MatchInfoPort;
import com.hoops.participation.domain.model.Participation;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ParticipationJoiner implements ParticipateInMatchUseCase {

    private final MatchInfoPort matchInfoPort;
    private final ParticipationValidator validator;
    private final ParticipationCreator creator;

    @Override
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            noRetryFor = BusinessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public Participation participateInMatch(ParticipateInMatchCommand command) {
        MatchInfo matchInfo = matchInfoPort.getMatchInfo(command.matchId());
        validator.validateForParticipation(matchInfo, command.userId());
        return creator.create(command, matchInfo);
    }

    @Recover
    public Participation recoverFromConflict(OptimisticLockingFailureException e, ParticipateInMatchCommand command) {
        throw new ParticipationConflictException(command.matchId());
    }

    @Recover
    public Participation recoverFromBusinessException(BusinessException e, ParticipateInMatchCommand command) {
        throw e;
    }
}
