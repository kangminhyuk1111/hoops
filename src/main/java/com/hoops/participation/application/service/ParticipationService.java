package com.hoops.participation.application.service;

import com.hoops.common.event.ParticipationCancelledEvent;
import com.hoops.common.event.ParticipationCreatedEvent;
import com.hoops.common.exception.BusinessException;
import com.hoops.participation.application.exception.AlreadyParticipatingException;
import com.hoops.participation.application.exception.CancellationConflictException;
import com.hoops.participation.application.exception.HostCannotParticipateException;
import com.hoops.participation.application.exception.InvalidMatchStatusException;
import com.hoops.participation.application.exception.InvalidParticipationStatusException;
import com.hoops.participation.application.exception.MatchAlreadyStartedException;
import com.hoops.participation.application.exception.MatchFullException;
import com.hoops.participation.application.exception.NotHostException;
import com.hoops.participation.application.exception.NotParticipantException;
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

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ParticipationService implements ParticipateInMatchUseCase, CancelParticipationUseCase,
        ApproveParticipationUseCase, RejectParticipationUseCase, GetMyParticipationsUseCase, GetMatchParticipantsUseCase {

    private final ParticipationRepository participationRepository;
    private final MatchInfoProvider matchInfoProvider;
    private final ParticipationEventPublisher eventPublisher;

    @Override
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            noRetryFor = BusinessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public Participation participateInMatch(ParticipateInMatchCommand command) {
        MatchInfo matchInfo = matchInfoProvider.getMatchInfo(command.matchId());

        validateParticipation(matchInfo, command.userId());

        Participation participation = new Participation(
                null,
                command.matchId(),
                command.userId(),
                ParticipationStatus.PENDING,
                LocalDateTime.now()
        );

        Participation savedParticipation = participationRepository.save(participation);

        matchInfoProvider.addParticipant(command.matchId());

        Participation confirmedParticipation = new Participation(
                savedParticipation.getId(),
                savedParticipation.getMatchId(),
                savedParticipation.getUserId(),
                ParticipationStatus.CONFIRMED,
                savedParticipation.getJoinedAt()
        );

        Participation result = participationRepository.save(confirmedParticipation);

        eventPublisher.publish(new ParticipationCreatedEvent(
                result.getId(),
                result.getMatchId(),
                result.getUserId(),
                matchInfo.title()
        ));

        return result;
    }

    private void validateParticipation(MatchInfo matchInfo, Long userId) {
        if (matchInfo.isHost(userId)) {
            throw new HostCannotParticipateException(matchInfo.matchId(), userId);
        }

        if (!matchInfo.canParticipate()) {
            if (matchInfo.isFull()) {
                throw new MatchFullException(matchInfo.matchId());
            }
            throw new InvalidMatchStatusException(matchInfo.matchId(), matchInfo.status());
        }

        if (participationRepository.existsByMatchIdAndUserId(matchInfo.matchId(), userId)) {
            throw new AlreadyParticipatingException(matchInfo.matchId(), userId);
        }
    }

    @Override
    @Retryable(
            retryFor = OptimisticLockingFailureException.class,
            noRetryFor = BusinessException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void cancelParticipation(CancelParticipationCommand command) {
        Participation participation = participationRepository.findById(command.participationId())
                .orElseThrow(() -> new ParticipationNotFoundException(command.participationId()));

        if (!participation.isOwner(command.userId())) {
            throw new NotParticipantException(command.participationId(), command.userId());
        }

        if (!participation.canCancel()) {
            throw new InvalidParticipationStatusException(
                    command.participationId(),
                    participation.getStatus().name());
        }

        MatchInfo matchInfo = matchInfoProvider.getMatchInfo(command.matchId());
        if (matchInfo.hasStarted()) {
            throw new MatchAlreadyStartedException(command.matchId());
        }

        Participation cancelledParticipation = participation.cancel();
        participationRepository.save(cancelledParticipation);

        matchInfoProvider.removeParticipant(command.matchId());

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
    public Participation approveParticipation(ApproveParticipationCommand command) {
        MatchInfo matchInfo = matchInfoProvider.getMatchInfo(command.matchId());

        validateHostPermission(matchInfo, command.hostUserId());

        Participation participation = participationRepository.findById(command.participationId())
                .orElseThrow(() -> new ParticipationNotFoundException(command.participationId()));

        if (!participation.canBeApprovedOrRejected()) {
            throw new InvalidParticipationStatusException(
                    command.participationId(),
                    participation.getStatus().name());
        }

        Participation approvedParticipation = participation.approve();
        return participationRepository.save(approvedParticipation);
    }

    @Override
    public Participation rejectParticipation(RejectParticipationCommand command) {
        MatchInfo matchInfo = matchInfoProvider.getMatchInfo(command.matchId());

        validateHostPermission(matchInfo, command.hostUserId());

        Participation participation = participationRepository.findById(command.participationId())
                .orElseThrow(() -> new ParticipationNotFoundException(command.participationId()));

        if (!participation.canBeApprovedOrRejected()) {
            throw new InvalidParticipationStatusException(
                    command.participationId(),
                    participation.getStatus().name());
        }

        Participation rejectedParticipation = participation.reject();
        matchInfoProvider.removeParticipant(command.matchId());
        return participationRepository.save(rejectedParticipation);
    }

    private void validateHostPermission(MatchInfo matchInfo, Long userId) {
        if (!matchInfo.isHost(userId)) {
            throw new NotHostException(matchInfo.matchId(), userId);
        }
    }
}
