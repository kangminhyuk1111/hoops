package com.hoops.participation.application.service;

import com.hoops.participation.application.exception.AlreadyParticipatingException;
import com.hoops.participation.application.exception.CancelTimeExceededException;
import com.hoops.participation.application.exception.HostCannotParticipateException;
import com.hoops.participation.application.exception.InvalidMatchStatusException;
import com.hoops.participation.application.exception.MatchAlreadyStartedException;
import com.hoops.participation.application.exception.MatchFullException;
import com.hoops.participation.application.exception.NotHostException;
import com.hoops.participation.application.exception.NotParticipantException;
import com.hoops.participation.application.exception.OverlappingParticipationException;
import com.hoops.participation.application.port.out.MatchInfo;
import com.hoops.participation.application.port.out.MatchInfoPort;
import com.hoops.participation.domain.model.Participation;
import com.hoops.participation.domain.repository.ParticipationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 참가 관련 검증 로직을 담당하는 Validator
 */
@Component
@RequiredArgsConstructor
public class ParticipationValidator {

    private final ParticipationRepository participationRepository;
    private final MatchInfoPort matchInfoProvider;

    public void validateForParticipation(MatchInfo matchInfo, Long userId) {
        validateNotHost(matchInfo, userId);
        validateMatchCanAcceptParticipant(matchInfo);
        validateNotAlreadyParticipating(matchInfo.matchId(), userId);
        validateNoOverlappingParticipation(matchInfo, userId);
    }

    public void validateForCancellation(Participation participation, MatchInfo matchInfo, Long userId) {
        validateIsOwner(participation, userId);
        // Entity validates state in cancel() method - removed duplicate check
        validateMatchNotStarted(matchInfo);
        validateCancelTimeNotExceeded(matchInfo, participation);
    }

    public void validateHostPermission(MatchInfo matchInfo, Long userId) {
        if (!matchInfo.isHost(userId)) {
            throw new NotHostException(matchInfo.matchId(), userId);
        }
    }

    private void validateNotHost(MatchInfo matchInfo, Long userId) {
        if (matchInfo.isHost(userId)) {
            throw new HostCannotParticipateException(matchInfo.matchId(), userId);
        }
    }

    private void validateMatchCanAcceptParticipant(MatchInfo matchInfo) {
        if (!matchInfo.canParticipate()) {
            if (matchInfo.isFull()) {
                throw new MatchFullException(matchInfo.matchId());
            }
            throw new InvalidMatchStatusException(matchInfo.matchId(), matchInfo.status());
        }
    }

    private void validateNotAlreadyParticipating(Long matchId, Long userId) {
        if (participationRepository.existsActiveParticipation(matchId, userId)) {
            throw new AlreadyParticipatingException(matchId, userId);
        }
    }

    private void validateNoOverlappingParticipation(MatchInfo targetMatch, Long userId) {
        List<Participation> userParticipations = participationRepository.findActiveParticipationsByUserId(userId);
        if (userParticipations.isEmpty()) {
            return;
        }

        List<Long> matchIds = userParticipations.stream()
                .map(Participation::getMatchId)
                .toList();

        List<MatchInfo> existingMatches = matchInfoProvider.getMatchInfoByIds(matchIds);
        for (MatchInfo existingMatch : existingMatches) {
            if (existingMatch.overlapsWithTime(targetMatch.getStartDateTime(), targetMatch.getEndDateTime())) {
                throw new OverlappingParticipationException(targetMatch.matchId(), existingMatch.matchId());
            }
        }
    }

    private void validateIsOwner(Participation participation, Long userId) {
        if (!participation.isOwner(userId)) {
            throw new NotParticipantException(participation.getId(), userId);
        }
    }

    private void validateMatchNotStarted(MatchInfo matchInfo) {
        if (matchInfo.hasStarted()) {
            throw new MatchAlreadyStartedException(matchInfo.matchId());
        }
    }

    private void validateCancelTimeNotExceeded(MatchInfo matchInfo, Participation participation) {
        if (!matchInfo.canCancelByTime()) {
            throw new CancelTimeExceededException(participation.getId());
        }
    }
}
