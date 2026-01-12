package com.hoops.user.adapter.in.web;

import com.hoops.participation.adapter.in.web.dto.ParticipationResponse;
import com.hoops.participation.application.port.in.GetMyParticipationsUseCase;
import com.hoops.participation.domain.Participation;
import com.hoops.user.adapter.in.web.dto.UserResponse;
import com.hoops.user.application.port.in.GetMyProfileUseCase;
import com.hoops.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetMyProfileUseCase getMyProfileUseCase;
    private final GetMyParticipationsUseCase getMyParticipationsUseCase;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal Long userId) {
        User user = getMyProfileUseCase.getMyProfile(userId);
        return ResponseEntity.ok(UserResponse.of(user));
    }

    @GetMapping("/me/participations")
    public ResponseEntity<List<ParticipationResponse>> getMyParticipations(@AuthenticationPrincipal Long userId) {
        List<Participation> participations = getMyParticipationsUseCase.getMyParticipations(userId);
        List<ParticipationResponse> response = participations.stream()
                .map(ParticipationResponse::of)
                .toList();
        return ResponseEntity.ok(response);
    }
}
