package com.hoops.participation.application.service;

import com.hoops.common.exception.BusinessException;
import com.hoops.participation.application.exception.CancellationConflictException;
import com.hoops.participation.application.port.in.CancelParticipationCommand;
import com.hoops.participation.application.port.in.CancelParticipationUseCase;
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
public class ParticipationLeaver implements CancelParticipationUseCase {

    private final MatchInfoPort matchInfoPort;
    private final ParticipationValidator validator;
    private final ParticipationFinder finder;
    private final ParticipationCanceller canceller;

    @Override
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            noRetryFor = BusinessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void cancelParticipation(CancelParticipationCommand command) {
        Participation participation = finder.findById(command.participationId());
        MatchInfo matchInfo = matchInfoPort.getMatchInfo(command.matchId());
        validator.validateForCancellation(participation, matchInfo, command.userId());
        canceller.cancel(participation, matchInfo, command.matchId());
    }

    @Recover
    public void recoverFromConflict(OptimisticLockingFailureException e, CancelParticipationCommand command) {
        throw new CancellationConflictException(command.matchId());
    }

    @Recover
    public void recoverFromBusinessException(BusinessException e, CancelParticipationCommand command) {
        throw e;
    }
}
