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
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 경기 참가 취소 Step Definitions
 */
public class ParticipationCancelStepDefs {

    private final TestAdapter testAdapter;
    private final MatchRepository matchRepository;
    private final UserRepository userRepository;
    private final ParticipationRepository participationRepository;
    private final SharedTestContext sharedContext;

    private Participation myParticipation;
    private Participation otherParticipation;

    public ParticipationCancelStepDefs(
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

    @그리고("내가 해당 경기에 참가 승인된 상태이다")
    public void 내가_해당_경기에_참가_승인된_상태이다() {
        User testUser = sharedContext.getTestUser();
        Match match = sharedContext.getTestMatches().get(0);

        Participation participation = Participation.builder()
                .matchId(match.getId())
                .userId(testUser.getId())
                .status(ParticipationStatus.CONFIRMED)
                .joinedAt(LocalDateTime.now())
                .build();
        myParticipation = participationRepository.save(participation);
    }

    @그리고("내가 해당 경기에 참가 대기 중인 상태이다")
    public void 내가_해당_경기에_참가_대기_중인_상태이다() {
        User testUser = sharedContext.getTestUser();
        Match match = sharedContext.getTestMatches().get(0);

        Participation participation = Participation.builder()
                .matchId(match.getId())
                .userId(testUser.getId())
                .status(ParticipationStatus.PENDING)
                .joinedAt(LocalDateTime.now())
                .build();
        myParticipation = participationRepository.save(participation);
    }

    @그리고("다른 참가자가 해당 경기에 참가 승인된 상태이다")
    public void 다른_참가자가_해당_경기에_참가_승인된_상태이다() {
        User otherUser = createOtherUser("otherparticipant");
        Match match = sharedContext.getTestMatches().get(0);

        Participation participation = Participation.builder()
                .matchId(match.getId())
                .userId(otherUser.getId())
                .status(ParticipationStatus.CONFIRMED)
                .joinedAt(LocalDateTime.now())
                .build();
        otherParticipation = participationRepository.save(participation);
    }

    @만일("해당 경기 참가 취소 API를 호출한다")
    public void 해당_경기_참가_취소_API를_호출한다() {
        Match match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();

        String path = "/api/matches/" + match.getId() + "/participations/" + myParticipation.getId();
        TestResponse response;
        if (accessToken != null) {
            response = testAdapter.deleteWithAuth(path, accessToken);
        } else {
            response = testAdapter.deleteWithAuth(path, null);
        }
        sharedContext.setLastResponse(response);
    }

    @만일("다른 참가자의 참가 취소 API를 호출한다")
    public void 다른_참가자의_참가_취소_API를_호출한다() {
        Match match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();

        String path = "/api/matches/" + match.getId() + "/participations/" + otherParticipation.getId();
        TestResponse response = testAdapter.deleteWithAuth(path, accessToken);
        sharedContext.setLastResponse(response);
    }

    @만일("존재하지 않는 참가 취소 API를 호출한다")
    public void 존재하지_않는_참가_취소_API를_호출한다() {
        Match match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();
        Long nonExistentParticipationId = 999999L;

        String path = "/api/matches/" + match.getId() + "/participations/" + nonExistentParticipationId;
        TestResponse response = testAdapter.deleteWithAuth(path, accessToken);
        sharedContext.setLastResponse(response);
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
