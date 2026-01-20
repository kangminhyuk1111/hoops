package com.hoops.match.application.port.in;

import com.hoops.match.domain.model.Match;

/**
 * 경기 수정 유스케이스
 */
public interface UpdateMatchUseCase {

    /**
     * 경기를 수정합니다.
     *
     * @param command 경기 수정 커맨드
     * @return 수정된 경기
     * @throws com.hoops.match.application.exception.MatchNotFoundException 경기를 찾을 수 없는 경우
     * @throws com.hoops.match.application.exception.NotMatchHostException 호스트가 아닌 경우
     * @throws com.hoops.match.application.exception.MatchCannotBeUpdatedException 수정할 수 없는 상태인 경우
     */
    Match updateMatch(UpdateMatchCommand command);
}
