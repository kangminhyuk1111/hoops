package com.hoops.user.application.service;

import com.hoops.user.application.exception.UserNotFoundException;
import com.hoops.user.application.port.in.GetMyProfileUseCase;
import com.hoops.user.domain.User;
import com.hoops.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserProfileService implements GetMyProfileUseCase {

    private final UserRepository userRepository;

    @Override
    public User getMyProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}
