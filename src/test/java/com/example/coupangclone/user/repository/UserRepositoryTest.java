package com.example.coupangclone.user.repository;

import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.enums.UserRoleEnum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @DisplayName("이메일로 사용자 조회 성공한다.")
    @Test
    void findByEmail_success(){
        // given
        User user = User.builder()
                .email("test123@example.com")
                .password("qwer123!")
                .name("홍길동")
                .tel("01012345678")
                .gender("남성")
                .role(UserRoleEnum.USER)
                .build();
        userRepository.save(user);

        // when
        Optional<User> result = userRepository.findByEmail("test123@example.com");

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("test123@example.com");
    }

    @DisplayName("존재하지 않는 이메일 조회시 실패한다.")
    @Test
    void findByEmail_fail(){
        // given
        Optional<User> result = userRepository.findByEmail("fail123@example.com");

        // when // then
        assertThat(result).isNotPresent();
    }

}