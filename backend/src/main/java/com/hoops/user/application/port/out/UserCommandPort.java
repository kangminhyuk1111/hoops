package com.hoops.user.application.port.out;

/**
 * 외부 Context에 제공하는 사용자 생성/수정 포트
 *
 * User Context 외부에서 사용자를 생성하거나 수정할 때 사용합니다.
 * 내부 Repository를 직접 노출하지 않고, 필요한 기능만 제공합니다.
 */
public interface UserCommandPort {

    /**
     * 새로운 사용자를 생성합니다.
     *
     * @param command 사용자 생성 명령
     * @return 생성된 사용자 정보
     */
    UserQueryPort.UserDetails createUser(CreateUserCommand command);

    /**
     * 닉네임 중복 여부를 확인합니다.
     *
     * @param nickname 확인할 닉네임
     * @return 중복 여부
     */
    boolean existsByNickname(String nickname);

    /**
     * 사용자 생성 명령
     */
    record CreateUserCommand(
            String email,
            String nickname,
            String profileImage
    ) {
        public static CreateUserCommand of(String email, String nickname, String profileImage) {
            return new CreateUserCommand(email, nickname, profileImage);
        }
    }
}
