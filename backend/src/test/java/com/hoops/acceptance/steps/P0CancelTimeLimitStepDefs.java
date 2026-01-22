package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.match.domain.repository.MatchRepository;
import com.hoops.match.domain.model.Match;
import com.hoops.match.domain.vo.MatchStatus;
import com.hoops.participation.domain.model.Participation;
import com.hoops.participation.domain.vo.ParticipationStatus;
import com.hoops.participation.domain.repository.ParticipationRepository;
import com.hoops.user.domain.model.User;
import com.hoops.user.domain.repository.UserRepository;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * P0 취소 시간 제한 Step Definitions
 */
public class P0CancelTimeLimitStepDefs {

    private final TestAdapter testAdapter;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    private final SharedTestContext sharedContext;

    private Participation myParticipation;

    public P0CancelTimeLimitStepDefs(
            TestAdapter testAdapter,
            MatchRepository matchRepository,
            UserRepository userRepository,
            ParticipationRepository participationRepository,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.matchRepository = matchRepository;
        this.userRepository = userRepository;
        this.participationRepository = participationRepository;
        this.sharedContext = sharedContext;
    }

    @먼저("{int}시간 후에 시작하는 내 경기가 있다")
    public void N시간_후에_시작하는_내_경기가_있다(int hoursLater) {
        User testUser = sharedContext.getTestUser();
        sharedContext.clearTestMatches();

        LocalDateTime startDateTime = LocalDateTime.now().plusHours(hoursLater);
        LocalDate matchDate = startDateTime.toLocalDate();
        LocalTime startTime = startDateTime.toLocalTime();
        LocalTime endTime = startTime.plusHours(2);

        Match match = Match.reconstitute(
                null,  // id
                0L,    // version
                testUser.getId(),  // hostId
                testUser.getNickname(),  // hostNickname
                hoursLater + "시간 후 시작 경기",  // title
                "테스트 경기 설명",  // description
                BigDecimal.valueOf(37.5665),  // latitude
                BigDecimal.valueOf(126.9780),  // longitude
                "서울시 중구",  // address
                matchDate,  // matchDate
                startTime,  // startTime
                endTime,  // endTime
                10,  // maxParticipants
                1,   // currentParticipants
                MatchStatus.PENDING,  // status
                null  // cancelledAt
        );
        Match savedMatch = matchRepository.save(match);
        sharedContext.addTestMatch(savedMatch);
    }

    @먼저("{int}시간 후에 시작하는 다른 사용자의 경기가 있다")
    public void N시간_후에_시작하는_다른_사용자의_경기가_있다(int hoursLater) {
        User otherUser = createOtherUser("otherhost");
        sharedContext.setHostUser(otherUser);
        sharedContext.clearTestMatches();

        LocalDateTime startDateTime = LocalDateTime.now().plusHours(hoursLater);
        LocalDate matchDate = startDateTime.toLocalDate();
        LocalTime startTime = startDateTime.toLocalTime();
        LocalTime endTime = startTime.plusHours(2);

        Match match = Match.reconstitute(
                null,  // id
                0L,    // version
                otherUser.getId(),  // hostId
                otherUser.getNickname(),  // hostNickname
                hoursLater + "시간 후 시작 경기",  // title
                "테스트 경기 설명",  // description
                BigDecimal.valueOf(37.5665),  // latitude
                BigDecimal.valueOf(126.9780),  // longitude
                "서울시 중구",  // address
                matchDate,  // matchDate
                startTime,  // startTime
                endTime,  // endTime
                10,  // maxParticipants
                1,   // currentParticipants
                MatchStatus.PENDING,  // status
                null  // cancelledAt
        );
        Match savedMatch = matchRepository.save(match);
        sharedContext.addTestMatch(savedMatch);
    }

    @만일("취소 사유 {string}으로 경기 취소 API를 호출한다")
    public void 취소_사유로_경기_취소_API를_호출한다(String cancelReason) {
        Match match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();

        Map<String, Object> request = new HashMap<>();
        request.put("reason", cancelReason);

        TestResponse response = testAdapter.deleteWithAuthAndBody(
                "/api/matches/" + match.getId(),
                request,
                accessToken
        );
        sharedContext.setLastResponse(response);
    }

    @만일("취소 사유 없이 경기 취소 API를 호출한다")
    public void 취소_사유_없이_경기_취소_API를_호출한다() {
        Match match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();

        Map<String, Object> request = new HashMap<>();
        request.put("reason", "");

        TestResponse response = testAdapter.deleteWithAuthAndBody(
                "/api/matches/" + match.getId(),
                request,
                accessToken
        );
        sharedContext.setLastResponse(response);
    }

    @그리고("응답 에러 코드가 {string} 이다")
    public void 응답_에러_코드가_이다(String expectedErrorCode) {
        TestResponse response = sharedContext.getLastResponse();
        String errorCode = (String) response.getJsonValue("errorCode");
        assertThat(errorCode)
                .as("에러 코드가 %s 이어야 합니다", expectedErrorCode)
                .isEqualTo(expectedErrorCode);
    }

    private User createOtherUser(String prefix) {
        User user = User.builder()
                .email(prefix + System.currentTimeMillis() + "@example.com")
                .nickname(prefix + "사용자")
                .rating(BigDecimal.valueOf(3.0))
                .totalMatches(0)
                .build();
        return userRepository.save(user);
    }
}
