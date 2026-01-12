package com.hoops.user.application.service;

import com.hoops.user.application.exception.DuplicateNicknameException;
import com.hoops.user.application.exception.UserNotFoundException;
import com.hoops.user.application.port.in.GetMyProfileUseCase;
import com.hoops.user.application.port.in.GetUserProfileUseCase;
import com.hoops.user.application.port.in.UpdateUserProfileCommand;
import com.hoops.user.application.port.in.UpdateUserProfileUseCase;
import com.hoops.user.domain.User;
import com.hoops.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class UserProfileService implements GetMyProfileUseCase, GetUserProfileUseCase, UpdateUserProfileUseCase {

    private final UserRepository userRepository;

    @Override
    public User getMyProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    public User getUserProfile(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional
    public User updateUserProfile(UpdateUserProfileCommand command) {
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        // 닉네임 중복 체크 (현재 닉네임과 다른 경우만)
        if (command.nickname() != null && !command.nickname().equals(user.getNickname())) {
            if (userRepository.existsByNickname(command.nickname())) {
                throw new DuplicateNicknameException(command.nickname());
            }
        }

        User updatedUser = user.updateProfile(command.nickname(), command.profileImage());
        return userRepository.save(updatedUser);
    }
}
