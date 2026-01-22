package com.hoops.acceptance.steps;

import com.hoops.acceptance.adapter.TestAdapter;
import com.hoops.acceptance.adapter.TestResponse;
import com.hoops.match.adapter.out.persistence.MatchJpaEntity;
import com.hoops.match.adapter.out.persistence.SpringDataMatchRepository;
import com.hoops.match.domain.vo.MatchStatus;
import com.hoops.user.domain.model.User;
import com.hoops.user.domain.repository.UserRepository;
import io.cucumber.java.ko.먼저;
import io.cucumber.java.ko.만일;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class MatchReactivateStepDefs {

    private final TestAdapter testAdapter;
    private final SpringDataMatchRepository jpaMatchRepository;
    private final UserRepository userRepository;
    private final SharedTestContext sharedContext;

    public MatchReactivateStepDefs(
            TestAdapter testAdapter,
            SpringDataMatchRepository jpaMatchRepository,
            UserRepository userRepository,
            SharedTestContext sharedContext) {
        this.testAdapter = testAdapter;
        this.jpaMatchRepository = jpaMatchRepository;
        this.userRepository = userRepository;
        this.sharedContext = sharedContext;
    }

    @먼저("내가 생성한 취소된 경기가 있다")
    public void 내가_생성한_취소된_경기가_있다() {
        User testUser = sharedContext.getTestUser();
        sharedContext.clearTestMatches();

        MatchJpaEntity entity = new MatchJpaEntity(
                testUser.getId(),
                testUser.getNickname(),
                "내가 생성한 취소된 경기",
                "테스트 경기 설명",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780),
                "서울시 중구",
                LocalDate.now().plusDays(7),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                10,
                0,
                MatchStatus.CANCELLED
        );
        entity.setCancelledAt(LocalDateTime.now());
        MatchJpaEntity savedEntity = jpaMatchRepository.save(entity);

        sharedContext.addTestMatch(toMatch(savedEntity));
    }

    @먼저("다른 사용자가 생성한 취소된 경기가 있다")
    public void 다른_사용자가_생성한_취소된_경기가_있다() {
        User otherUser = User.builder()
                .email("other-reactivate@example.com")
                .nickname("다른사용자복구")
                .rating(BigDecimal.valueOf(3.0))
                .totalMatches(0)
                .build();
        otherUser = userRepository.save(otherUser);

        sharedContext.clearTestMatches();

        MatchJpaEntity entity = new MatchJpaEntity(
                otherUser.getId(),
                otherUser.getNickname(),
                "다른 사용자의 취소된 경기",
                "테스트 경기 설명",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780),
                "서울시 중구",
                LocalDate.now().plusDays(7),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                10,
                0,
                MatchStatus.CANCELLED
        );
        entity.setCancelledAt(LocalDateTime.now());
        MatchJpaEntity savedEntity = jpaMatchRepository.save(entity);

        sharedContext.addTestMatch(toMatch(savedEntity));
    }

    @먼저("1시간 전에 취소된 경기가 있다")
    public void 시간_전에_취소된_경기가_있다() {
        User testUser = sharedContext.getTestUser();
        sharedContext.clearTestMatches();

        MatchJpaEntity entity = new MatchJpaEntity(
                testUser.getId(),
                testUser.getNickname(),
                "1시간 전에 취소된 경기",
                "테스트 경기 설명",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780),
                "서울시 중구",
                LocalDate.now().plusDays(7),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                10,
                0,
                MatchStatus.CANCELLED
        );
        entity.setCancelledAt(LocalDateTime.now().minusHours(1).minusMinutes(1));
        MatchJpaEntity savedEntity = jpaMatchRepository.save(entity);

        sharedContext.addTestMatch(toMatch(savedEntity));
    }

    @먼저("경기 날짜가 지난 취소된 경기가 있다")
    public void 경기_날짜가_지난_취소된_경기가_있다() {
        User testUser = sharedContext.getTestUser();
        sharedContext.clearTestMatches();

        MatchJpaEntity entity = new MatchJpaEntity(
                testUser.getId(),
                testUser.getNickname(),
                "경기 날짜가 지난 취소된 경기",
                "테스트 경기 설명",
                BigDecimal.valueOf(37.5665),
                BigDecimal.valueOf(126.9780),
                "서울시 중구",
                LocalDate.now().minusDays(1),
                LocalTime.of(18, 0),
                LocalTime.of(20, 0),
                10,
                0,
                MatchStatus.CANCELLED
        );
        entity.setCancelledAt(LocalDateTime.now().minusMinutes(30));
        MatchJpaEntity savedEntity = jpaMatchRepository.save(entity);

        sharedContext.addTestMatch(toMatch(savedEntity));
    }

    @만일("해당 경기 복구 API를 호출한다")
    public void 해당_경기_복구_API를_호출한다() {
        var match = sharedContext.getTestMatches().get(0);
        String accessToken = sharedContext.getAccessToken();

        TestResponse response = testAdapter.postWithAuth(
                "/api/matches/" + match.getId() + "/reactivate",
                null,
                accessToken
        );
        sharedContext.setLastResponse(response);
    }

    @만일("존재하지 않는 경기 복구 API를 호출한다")
    public void 존재하지_않는_경기_복구_API를_호출한다() {
        String accessToken = sharedContext.getAccessToken();
        Long nonExistentMatchId = 999999L;

        TestResponse response = testAdapter.postWithAuth(
                "/api/matches/" + nonExistentMatchId + "/reactivate",
                null,
                accessToken
        );
        sharedContext.setLastResponse(response);
    }

    private com.hoops.match.domain.model.Match toMatch(MatchJpaEntity entity) {
        return com.hoops.match.domain.model.Match.reconstitute(
                entity.getId(),  // id
                entity.getVersion(),  // version
                entity.getHostId(),  // hostId
                entity.getHostNickname(),  // hostNickname
                entity.getTitle(),  // title
                entity.getDescription(),  // description
                entity.getLatitude(),  // latitude
                entity.getLongitude(),  // longitude
                entity.getAddress(),  // address
                entity.getMatchDate(),  // matchDate
                entity.getStartTime(),  // startTime
                entity.getEndTime(),  // endTime
                entity.getMaxParticipants(),  // maxParticipants
                entity.getCurrentParticipants(),  // currentParticipants
                entity.getStatus(),  // status
                entity.getCancelledAt()  // cancelledAt
        );
    }
}
