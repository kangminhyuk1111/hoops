package com.hoops.participation.application.service;

import com.hoops.common.exception.BusinessException;
import com.hoops.participation.application.exception.AlreadyParticipatingException;
import com.hoops.participation.application.exception.CancellationConflictException;
import com.hoops.participation.application.exception.HostCannotParticipateException;
import com.hoops.participation.application.exception.InvalidMatchStatusException;
import com.hoops.participation.application.exception.InvalidParticipationStatusException;
import com.hoops.participation.application.exception.MatchAlreadyStartedException;
import com.hoops.participation.application.exception.MatchFullException;
import com.hoops.participation.application.exception.NotParticipantException;
import com.hoops.participation.application.exception.ParticipationNotFoundException;
import com.hoops.participation.application.port.in.CancelParticipationCommand;
import com.hoops.participation.application.port.in.CancelParticipationUseCase;
import com.hoops.participation.application.port.in.GetMatchParticipantsUseCase;
import com.hoops.participation.application.port.in.GetMyParticipationsUseCase;
import com.hoops.participation.application.port.in.ParticipateInMatchCommand;
import com.hoops.participation.application.port.in.ParticipateInMatchUseCase;
import com.hoops.participation.application.port.out.MatchInfo;
import com.hoops.participation.application.port.out.MatchInfoProvider;
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
public class ParticipationService implements ParticipateInMatchUseCase, CancelParticipationUseCase, GetMyParticipationsUseCase, GetMatchParticipantsUseCase {

    private final ParticipationRepository participationRepository;
    private final MatchInfoProvider matchInfoProvider;

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

        return participationRepository.save(confirmedParticipation);
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
}
