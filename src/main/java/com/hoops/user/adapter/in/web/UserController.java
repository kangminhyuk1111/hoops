package com.hoops.user.adapter.in.web;

import com.hoops.user.adapter.in.web.dto.UserResponse;
import com.hoops.user.application.port.in.GetMyProfileUseCase;
import com.hoops.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final GetMyProfileUseCase getMyProfileUseCase;

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyProfile(@AuthenticationPrincipal Long userId) {
        User user = getMyProfileUseCase.getMyProfile(userId);
        return ResponseEntity.ok(UserResponse.of(user));
    }
}
