package com.example.coupangclone.item.repository;

import com.example.coupangclone.config.JpaAuditingConfig;
import com.example.coupangclone.entity.item.Brand;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.enums.UserRoleEnum;
import com.example.coupangclone.repository.item.BrandRepository;
import com.example.coupangclone.repository.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@ActiveProfiles("test")
@DataJpaTest
@Import(JpaAuditingConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BrandRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BrandRepository brandRepository;

    @DisplayName("이미 존재하는 브랜드라면 existsByName이 true를 반환한다.")
    @Test
    void existsBrand(){
        // given
        User user = User.builder()
                .email("admin@example.com")
                .password("qwer123!")
                .name("관리자")
                .tel("01012345678")
                .gender("남성")
                .role(UserRoleEnum.ADMIN)
                .build();
        userRepository.save(user);
        Brand brand = Brand.builder()
                .name("애플")
                .build();
        brandRepository.save(brand);

        // when
        boolean existsByName = brandRepository.existsByName("애플");

        // then
        assertThat(existsByName).isTrue();
    }

    @DisplayName("브랜드 이름으로 브랜드를 찾는다.")
    @Test
    void findByName(){
        // given
        User user = User.builder()
                .email("admin@example.com")
                .password("qwer123!")
                .name("관리자")
                .tel("01012345678")
                .gender("남성")
                .role(UserRoleEnum.ADMIN)
                .build();
        userRepository.save(user);
        Brand brand = Brand.builder()
                .name("애플")
                .build();
        brandRepository.save(brand);

        // when
        Optional<Brand> result = brandRepository.findByName("애플");

        // then
        assertThat(result).isPresent();
    }

}