package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.auth.application.port.out.JwtTokenProvider;
import com.hoops.auth.domain.AuthAccount;
import com.hoops.auth.domain.AuthProvider;
import com.hoops.auth.domain.repository.AuthAccountRepository;
import com.hoops.location.domain.Location;
import com.hoops.location.domain.repository.LocationRepository;
import com.hoops.match.domain.Match;
import com.hoops.match.domain.MatchStatus;
import com.hoops.match.application.port.out.MatchRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import com.hoops.participation.domain.Participation;
import com.hoops.participation.domain.ParticipationStatus;
import com.hoops.participation.domain.repository.ParticipationRepository;
import com.hoops.user.domain.User;
import com.hoops.user.domain.repository.UserRepository;
import io.cucumber.java.ko.그리고;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 참가 승인/거절 기능 Step Definitions
 */
public class ParticipationApprovalStepDefs {

    private final TestAdapter testAdapter;
    private final UserRepository userRepository;
    private final AuthAccountRepository authAccountRepository;
    private final MatchRepository matchRepository;
    private final LocationRepository locationRepository;
    private final ParticipationRepository participationRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final SharedTestContext sharedContext;

    private Long participationId;
    private User participantUser;

    public ParticipationApprovalStepDefs(
            TestAdapter testAdapter,
            UserRepository userRepository,
            AuthAccountRepository authAccountRepository,
            MatchRepository matchRepository,
            LocationRepository locationRepository,
            ParticipationRepository participationRepository,
            JwtTokenProvider jwtTokenProvider,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.userRepository = userRepository;
        this.authAccountRepository = authAccountRepository;
        this.matchRepository = matchRepository;
        this.locationRepository = locationRepository;
        this.participationRepository = participationRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.sharedContext = sharedContext;
    }

    @먼저("호스트가 회원가입되어 있다")
    public void 호스트가_회원가입되어_있다() {
        String kakaoId = "host-kakao-" + UUID.randomUUID().toString().substring(0, 8);
        String email = "host" + System.currentTimeMillis() + "@kakao.com";

        User host = new User(
                null,
                email,
                "호스트유저",
                "https://example.com/host-profile.jpg",
                BigDecimal.valueOf(3.0),
                0
        );
        User savedHost = userRepository.save(host);
        sharedContext.setHostUser(savedHost);

        AuthAccount authAccount = new AuthAccount(
                null,
                savedHost.getId(),
                AuthProvider.KAKAO,
                kakaoId,
                null,
                null
        );
        authAccountRepository.save(authAccount);
    }

    @그리고("호스트가 로그인되어 있다")
    public void 호스트가_로그인되어_있다() {
        User host = sharedContext.getHostUser();
        String accessToken = jwtTokenProvider.createTokens(host.getId()).accessToken();
        sharedContext.setAccessToken(accessToken);
    }

    @그리고("호스트가 생성한 경기가 있다")
    public void 호스트가_생성한_경기가_있다() {
        User host = sharedContext.getHostUser();

        Location location = new Location(
                null,
                host.getId(),
                "테스트 농구장",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780),
                "서울시 강남구 테스트동 123"
        );
        Location savedLocation = locationRepository.save(location);

        LocalDate matchDate = LocalDate.now().plusDays(1);
        LocalTime startTime = LocalTime.of(18, 0);
        LocalTime endTime = LocalTime.of(20, 0);

        Match match = new Match(
                null,
                null,
                host.getId(),
                host.getNickname(),
                "테스트 경기",
                "테스트 경기입니다",
                savedLocation.getLatitude(),
                savedLocation.getLongitude(),
                savedLocation.getAddress(),
                matchDate,
                startTime,
                endTime,
                10,
                1,
                MatchStatus.PENDING
        );
        Match savedMatch = matchRepository.save(match);
        sharedContext.addTestMatch(savedMatch);
    }

    @먼저("다른 사용자가 해당 경기에 참가 신청했다")
    public void 다른_사용자가_해당_경기에_참가_신청했다() {
        String email = "participant" + System.currentTimeMillis() + "@kakao.com";

        participantUser = new User(
                null,
                email,
                "참가자유저",
                "https://example.com/participant-profile.jpg",
                BigDecimal.valueOf(3.0),
                0
        );
        participantUser = userRepository.save(participantUser);

        Match match = sharedContext.getTestMatches().get(0);

        Participation participation = new Participation(
                null,
                match.getId(),
                participantUser.getId(),
                ParticipationStatus.PENDING,
                LocalDateTime.now()
        );
        Participation savedParticipation = participationRepository.save(participation);
        participationId = savedParticipation.getId();
    }

    @만일("호스트가 해당 참가 신청을 승인한다")
    public void 호스트가_해당_참가_신청을_승인한다() {
        Match match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();

        String path = "/api/matches/" + match.getId() + "/participations/" + participationId + "/approve";
        TestResponse response = testAdapter.putWithAuth(path, null, accessToken);
        sharedContext.setLastResponse(response);
    }

    @만일("호스트가 해당 참가 신청을 거절한다")
    public void 호스트가_해당_참가_신청을_거절한다() {
        Match match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();

        String path = "/api/matches/" + match.getId() + "/participations/" + participationId + "/reject";
        TestResponse response = testAdapter.putWithAuth(path, null, accessToken);
        sharedContext.setLastResponse(response);
    }

    @그리고("참가 상태가 CONFIRMED 이다")
    public void 참가_상태가_CONFIRMED_이다() {
        TestResponse lastResponse = sharedContext.getLastResponse();
        assertThat(lastResponse.getJsonValue("status"))
                .as("참가 상태가 CONFIRMED 이어야 합니다")
                .isEqualTo("CONFIRMED");
    }

    @그리고("참가 상태가 REJECTED 이다")
    public void 참가_상태가_REJECTED_이다() {
        TestResponse lastResponse = sharedContext.getLastResponse();
        assertThat(lastResponse.getJsonValue("status"))
                .as("참가 상태가 REJECTED 이어야 합니다")
                .isEqualTo("REJECTED");
    }
}
