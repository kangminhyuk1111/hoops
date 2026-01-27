package com.hoops.participation.application.service;

import com.hoops.common.exception.BusinessException;
import com.hoops.participation.application.exception.ParticipationConflictException;
import com.hoops.participation.application.port.in.ApproveParticipationCommand;
import com.hoops.participation.application.port.in.ApproveParticipationUseCase;
import com.hoops.participation.application.port.in.RejectParticipationCommand;
import com.hoops.participation.application.port.in.RejectParticipationUseCase;
import com.hoops.participation.application.port.out.MatchInfo;
import com.hoops.participation.application.port.out.MatchInfoPort;
import com.hoops.participation.domain.model.Participation;
import com.hoops.participation.application.port.out.ParticipationRepositoryPort;
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
public class ParticipationApprover implements ApproveParticipationUseCase, RejectParticipationUseCase {

    private final ParticipationRepositoryPort participationRepository;
    private final MatchInfoPort matchInfoPort;
    private final ParticipationValidator validator;
    private final ParticipationFinder finder;

    @Override
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            noRetryFor = BusinessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public Participation approveParticipation(ApproveParticipationCommand command) {
        MatchInfo matchInfo = matchInfoPort.getMatchInfo(command.matchId());
        Participation participation = finder.findById(command.participationId());

        validator.validateHostPermission(matchInfo, command.hostUserId());

        Participation approved = participationRepository.save(participation.approve());
        matchInfoPort.addParticipant(command.matchId());
        return approved;
    }

    @Override
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            noRetryFor = BusinessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public Participation rejectParticipation(RejectParticipationCommand command) {
        MatchInfo matchInfo = matchInfoPort.getMatchInfo(command.matchId());
        Participation participation = finder.findById(command.participationId());

        validator.validateHostPermission(matchInfo, command.hostUserId());

        return participationRepository.save(participation.reject());
    }

    @Recover
    public Participation recoverFromConflict(OptimisticLockingFailureException e, ApproveParticipationCommand command) {
        throw new ParticipationConflictException(command.matchId());
    }

    @Recover
    public Participation recoverFromConflict(OptimisticLockingFailureException e, RejectParticipationCommand command) {
        throw new ParticipationConflictException(command.matchId());
    }

    @Recover
    public Participation recoverFromBusinessException(BusinessException e, ApproveParticipationCommand command) {
        throw e;
    }

    @Recover
    public Participation recoverFromBusinessException(BusinessException e, RejectParticipationCommand command) {
        throw e;
    }
}
