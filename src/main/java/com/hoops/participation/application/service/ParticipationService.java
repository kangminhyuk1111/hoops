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
import com.hoops.participation.application.port.in.ParticipateInMatchCommand;
import com.hoops.participation.application.port.in.ParticipateInMatchUseCase;
import com.hoops.participation.application.port.out.MatchInfo;
import com.hoops.participation.application.port.out.MatchInfoProvider;
import com.hoops.participation.domain.Participation;
import com.hoops.participation.domain.ParticipationStatus;
import com.hoops.participation.domain.repository.ParticipationRepository;
import java.time.LocalDateTime;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 경기 참가 서비스
 *
 * ParticipateInMatchUseCase, CancelParticipationUseCase를 구현하여
 * 경기 참가 및 취소 비즈니스 로직을 처리합니다.
 */
@Service
@Transactional
public class ParticipationService implements ParticipateInMatchUseCase, CancelParticipationUseCase {

    private final ParticipationRepository participationRepository;
    private final MatchInfoProvider matchInfoProvider;

    public ParticipationService(
            ParticipationRepository participationRepository,
            MatchInfoProvider matchInfoProvider) {
        this.participationRepository = participationRepository;
        this.matchInfoProvider = matchInfoProvider;
    }

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

        // 1. 참가 정보 생성 (PENDING)
        Participation participation = new Participation(
                null,
                command.matchId(),
                command.userId(),
                ParticipationStatus.PENDING,
                LocalDateTime.now()
        );

        // 2. 참가 정보 저장
        Participation savedParticipation = participationRepository.save(participation);

        // 3. Match에 참가자 추가 요청
        matchInfoProvider.addParticipant(command.matchId());

        // 4. 참가 상태를 CONFIRMED로 업데이트
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
        // 호스트는 자신의 경기에 참가 불가
        if (matchInfo.isHost(userId)) {
            throw new HostCannotParticipateException(matchInfo.matchId(), userId);
        }

        // 참가 가능한 경기 확인
        if (!matchInfo.canParticipate()) {
            if (matchInfo.isFull()) {
                throw new MatchFullException(matchInfo.matchId());
            }
            throw new InvalidMatchStatusException(matchInfo.matchId(), matchInfo.status());
        }

        // 중복 참가 확인
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
        // 1. 참가 정보 조회
        Participation participation = participationRepository.findById(command.participationId())
                .orElseThrow(() -> new ParticipationNotFoundException(command.participationId()));

        // 2. 본인 확인
        if (!participation.isOwner(command.userId())) {
            throw new NotParticipantException(command.participationId(), command.userId());
        }

        // 3. 취소 가능 상태 확인
        if (!participation.canCancel()) {
            throw new InvalidParticipationStatusException(
                    command.participationId(),
                    participation.getStatus().name());
        }

        // 4. 매치 정보 조회 및 경기 시작 여부 확인
        MatchInfo matchInfo = matchInfoProvider.getMatchInfo(command.matchId());
        if (matchInfo.hasStarted()) {
            throw new MatchAlreadyStartedException(command.matchId());
        }

        // 5. 참가 상태를 CANCELLED로 변경
        Participation cancelledParticipation = participation.cancel();
        participationRepository.save(cancelledParticipation);

        // 6. Match에서 참가자 제거 요청
        matchInfoProvider.removeParticipant(command.matchId());
    }

    @Recover
    public void recoverFromCancellationConflict(
            OptimisticLockingFailureException e,
            CancelParticipationCommand command) {
        throw new CancellationConflictException(command.matchId());
    }

    /**
     * 취소 비즈니스 예외에 대한 복구 처리
     * 재시도 대상이 아닌 예외는 그대로 다시 던진다
     */
    @Recover
    public void recoverFromCancellationBusinessException(
            BusinessException e,
            CancelParticipationCommand command) {
        throw e;
    }

    /**
     * 참가 비즈니스 예외에 대한 복구 처리
     * 재시도 대상이 아닌 예외는 그대로 다시 던진다
     */
    @Recover
    public Participation recoverFromParticipationBusinessException(
            BusinessException e,
            ParticipateInMatchCommand command) {
        throw e;
    }
}
