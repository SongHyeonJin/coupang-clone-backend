package com.example.coupangclone.item.repository;

import com.example.coupangclone.item.entity.Category;
import com.example.coupangclone.item.enums.ItemTypeEnum;
import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.enums.UserRoleEnum;
import com.example.coupangclone.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("이미 존재하는 카테고리라면 existsByName이 true를 반환한다.")
    @Test
    void existsCategory(){
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
        Category category = Category.builder()
                .name("전자제품")
                .type(ItemTypeEnum.THING)
                .parent(null)
                .build();
        categoryRepository.save(category);

        // when
        boolean existsByName = categoryRepository.existsByName("전자제품");

        // then
        assertThat(existsByName).isTrue();
    }

}