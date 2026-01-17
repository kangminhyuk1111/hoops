package com.hoops.participation.application.service;

import com.hoops.common.event.ParticipationCancelledEvent;
import com.hoops.common.event.ParticipationCreatedEvent;
import com.hoops.common.exception.BusinessException;
import com.hoops.participation.application.exception.CancellationConflictException;
import com.hoops.participation.application.exception.ParticipationConflictException;
import com.hoops.participation.application.exception.ParticipationNotFoundException;
import com.hoops.participation.application.port.in.ApproveParticipationCommand;
import com.hoops.participation.application.port.in.ApproveParticipationUseCase;
import com.hoops.participation.application.port.in.CancelParticipationCommand;
import com.hoops.participation.application.port.in.CancelParticipationUseCase;
import com.hoops.participation.application.port.in.GetMatchParticipantsUseCase;
import com.hoops.participation.application.port.in.GetMyParticipationsUseCase;
import com.hoops.participation.application.port.in.ParticipateInMatchCommand;
import com.hoops.participation.application.port.in.ParticipateInMatchUseCase;
import com.hoops.participation.application.port.in.RejectParticipationCommand;
import com.hoops.participation.application.port.in.RejectParticipationUseCase;
import com.hoops.participation.application.port.out.MatchInfo;
import com.hoops.participation.application.port.out.MatchInfoProvider;
import com.hoops.participation.application.port.out.ParticipationEventPublisher;
import com.hoops.participation.domain.Participation;
import com.hoops.participation.domain.ParticipationStatus;
import com.hoops.participation.domain.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ParticipationService implements ParticipateInMatchUseCase, CancelParticipationUseCase,
        ApproveParticipationUseCase, RejectParticipationUseCase, GetMyParticipationsUseCase, GetMatchParticipantsUseCase {

    private final ParticipationRepository participationRepository;
    private final MatchInfoProvider matchInfoProvider;
    private final ParticipationEventPublisher eventPublisher;
    private final ParticipationValidator validator;

    @Override
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            noRetryFor = BusinessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public Participation participateInMatch(ParticipateInMatchCommand command) {
        MatchInfo matchInfo = matchInfoProvider.getMatchInfo(command.matchId());
        validator.validateForParticipation(matchInfo, command.userId());

        Participation participation = findOrCreateParticipation(command);
        publishParticipationCreatedEvent(participation, matchInfo);

        return participation;
    }

    @Override
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            noRetryFor = BusinessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void cancelParticipation(CancelParticipationCommand command) {
        Participation participation = findParticipation(command.participationId());
        MatchInfo matchInfo = matchInfoProvider.getMatchInfo(command.matchId());
        validator.validateForCancellation(participation, matchInfo, command.userId());

        processCancellation(participation, matchInfo, command.matchId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Participation> getMyParticipations(Long userId) {
        return participationRepository.findByUserIdAndNotCancelled(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Participation> getMatchParticipants(Long matchId) {
        matchInfoProvider.getMatchInfo(matchId);
        return participationRepository.findByMatchIdAndNotCancelled(matchId);
    }

    @Override
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            noRetryFor = BusinessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public Participation approveParticipation(ApproveParticipationCommand command) {
        MatchInfo matchInfo = matchInfoProvider.getMatchInfo(command.matchId());
        Participation participation = findParticipation(command.participationId());

        validator.validateHostPermission(matchInfo, command.hostUserId());
        validator.validateCanBeApprovedOrRejected(participation);

        Participation approved = participationRepository.save(participation.approve());
        matchInfoProvider.addParticipant(command.matchId());

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
        MatchInfo matchInfo = matchInfoProvider.getMatchInfo(command.matchId());
        Participation participation = findParticipation(command.participationId());

        validator.validateHostPermission(matchInfo, command.hostUserId());
        validator.validateCanBeApprovedOrRejected(participation);

        return participationRepository.save(participation.reject());
    }

    private Participation findParticipation(Long participationId) {
        return participationRepository.findById(participationId)
                .orElseThrow(() -> new ParticipationNotFoundException(participationId));
    }

    private Participation findOrCreateParticipation(ParticipateInMatchCommand command) {
        return participationRepository
                .findCancelledParticipation(command.matchId(), command.userId())
                .map(cancelled -> participationRepository.save(cancelled.reactivate()))
                .orElseGet(() -> participationRepository.save(Participation.createPending(command.matchId(), command.userId())));
    }

    private void publishParticipationCreatedEvent(Participation participation, MatchInfo matchInfo) {
        eventPublisher.publish(new ParticipationCreatedEvent(
                participation.getId(),
                participation.getMatchId(),
                participation.getUserId(),
                matchInfo.title()
        ));
    }

    private void processCancellation(Participation participation, MatchInfo matchInfo, Long matchId) {
        boolean wasConfirmed = participation.getStatus() == ParticipationStatus.CONFIRMED;
        participationRepository.save(participation.cancel());

        if (wasConfirmed) {
            matchInfoProvider.removeParticipant(matchId);
        }

        eventPublisher.publish(new ParticipationCancelledEvent(
                participation.getId(),
                participation.getMatchId(),
                participation.getUserId(),
                matchInfo.title()
        ));
    }

    @Recover
    public void recoverFromCancellationConflict(
            OptimisticLockingFailureException e,
            CancelParticipationCommand command) {
        throw new CancellationConflictException(command.matchId());
    }

    @Recover
    public void recoverFromCancellationBusinessException(
            BusinessException e,
            CancelParticipationCommand command) {
        throw e;
    }

    @Recover
    public Participation recoverFromParticipationBusinessException(
            BusinessException e,
            ParticipateInMatchCommand command) {
        throw e;
    }

    @Recover
    public Participation recoverFromApproveConflict(
            OptimisticLockingFailureException e,
            ApproveParticipationCommand command) {
        throw new ParticipationConflictException(command.matchId());
    }

    @Recover
    public Participation recoverFromApproveBusinessException(
            BusinessException e,
            ApproveParticipationCommand command) {
        throw e;
    }

    @Recover
    public Participation recoverFromRejectConflict(
            OptimisticLockingFailureException e,
            RejectParticipationCommand command) {
        throw new ParticipationConflictException(command.matchId());
    }

    @Recover
    public Participation recoverFromRejectBusinessException(
            BusinessException e,
            RejectParticipationCommand command) {
        throw e;
    }
}
