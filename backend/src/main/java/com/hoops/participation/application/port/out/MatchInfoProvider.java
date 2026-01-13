package com.hoops.participation.application.port.out;

import java.util.List;

/**
 * 경기 정보 제공 포트
 *
 * Participation Context에서 Match 정보를 조회하기 위한 아웃바운드 포트입니다.
 * Match Context에 직접 의존하지 않고 이 인터페이스를 통해 필요한 정보만 조회합니다.
 */
public interface MatchInfoProvider {

    /**
     * 참가 검증을 위한 경기 정보를 조회합니다.
     *
     * @param matchId 경기 ID
     * @return 경기 정보
     * @throws com.hoops.participation.application.exception.ParticipationMatchNotFoundException 경기를 찾을 수 없는 경우
     */
    MatchInfo getMatchInfo(Long matchId);

    /**
     * 여러 경기 정보를 조회합니다.
     *
     * @param matchIds 경기 ID 목록
     * @return 경기 정보 목록
     */
    List<MatchInfo> getMatchInfoByIds(List<Long> matchIds);

    /**
     * 경기에 참가자를 추가합니다.
     *
     * @param matchId 경기 ID
     */
    void addParticipant(Long matchId);

    /**
     * 경기에서 참가자를 제거합니다.
     *
     * @param matchId 경기 ID
     */
    void removeParticipant(Long matchId);
}
