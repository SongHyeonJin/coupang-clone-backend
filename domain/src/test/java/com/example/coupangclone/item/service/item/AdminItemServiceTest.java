package com.example.coupangclone.item.service.item;

import com.example.coupangclone.entity.item.Brand;
import com.example.coupangclone.entity.item.Category;
import com.example.coupangclone.entity.item.command.BrandCommand;
import com.example.coupangclone.entity.item.command.CategoryCommand;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.enums.ItemTypeEnum;
import com.example.coupangclone.enums.UserRoleEnum;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.repository.item.BrandRepository;
import com.example.coupangclone.repository.item.CategoryRepository;
import com.example.coupangclone.repository.user.UserRepository;
import com.example.coupangclone.result.CategoryResult;
import com.example.coupangclone.service.item.AdminItemService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
class AdminItemServiceTest {

    @Autowired
    private AdminItemService adminItemService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;

    AdminItemServiceTest() {
    }

    @AfterEach
    void tearDown() {
        brandRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("관리자가 카테고리를 등록하는데 성공한다.")
    @Test
    void createCategory_success() {
        // given
        User admin =
                createUser("admin@example.com", "qwer123!", "관리자", "01098765432", "남성");
        userRepository.save(admin);
        CategoryCommand command = createCategoryDto("전자제품", ItemTypeEnum.THING, null);

        // when
        CategoryResult result = adminItemService.createCategory(command, admin);

        // then
        assertThat(result.name()).isEqualTo("전자제품");
        assertThat(categoryRepository.findAll()).hasSize(1);
    }

    @DisplayName("관리자가 아닌 회원이 카테고리를 등록하려하면 실패한다.")
    @Test
    void createCategory_fail_not_admin() {
        // given
        User user = User.builder()
                .email("user@example.com")
                .password("qwer123!")
                .name("회원")
                .tel("01032153215")
                .gender("여성")
                .role(UserRoleEnum.USER)
                .build();
        userRepository.save(user);
        CategoryCommand command = createCategoryDto("전자제품", ItemTypeEnum.THING, null);

        // when // then
        assertThatThrownBy(() -> adminItemService.createCategory(command, user))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.NOT_ALLOW.getMsg());
    }

    @DisplayName("존재하는 카테고리 등록 시 실패한다.")
    @Test
    void createCategory_fail_existsCategory() {
        // given
        User admin =
                createUser("admin@example.com", "qwer123!", "관리자", "01098765432", "남성");
        userRepository.save(admin);
        CategoryCommand command = createCategoryDto("전자제품", ItemTypeEnum.THING, null);
        Category category = createCategory(command.name(), command.type());
        categoryRepository.save(category);

        // when // then
        assertThatThrownBy(() -> adminItemService.createCategory(command, admin))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.CATEGORY_DUPLICATION.getMsg());
    }

    @DisplayName("카테고리의 상위 카테고리 등록 시 상위 카테고리가 존재하지 않으면 실패한다.")
    @Test
    void createCategory_fail_not_found_parentCategory() {
        // given
        User admin =
                createUser("admin@example.com", "qwer123!", "관리자", "01098765432", "남성");
        userRepository.save(admin);
        Category category = createCategory("과일", ItemTypeEnum.FOOD);
        categoryRepository.save(category);
        categoryRepository.deleteById(category.getId());
        CategoryCommand command = createCategoryDto("사과", ItemTypeEnum.FOOD, category.getId());

        // when // then
        assertThatThrownBy(() -> adminItemService.createCategory(command, admin))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.CATEGORY_NOT_FOUND.getMsg());
    }

    @DisplayName("관리자가 브랜드를 등록하는데 성공한다.")
    @Test
    void createBrand_success() {
        // given
        User admin =
                createUser("admin@example.com", "qwer123!", "관리자", "01098765432", "남성");
        userRepository.save(admin);
        BrandCommand command = BrandCommand.builder()
                .name("애플")
                .build();

        // when
        adminItemService.createBrand(command, admin);

        // then
        assertThat(brandRepository.findAll()).hasSize(1);
        assertThat(brandRepository.findAll().get(0).getName()).isEqualTo("애플");
    }

    @DisplayName("관리자가 아닌 회원이 브랜드를 등록하려하면 실패한다.")
    @Test
    void createBrand_fail_not_admin() {
        // given
        User user = User.builder()
                .email("user@example.com")
                .password("qwer123!")
                .name("회원")
                .tel("01032153215")
                .gender("여성")
                .role(UserRoleEnum.USER)
                .build();
        userRepository.save(user);
        BrandCommand command = BrandCommand.builder()
                .name("애플")
                .build();

        // when // then
        assertThatThrownBy(() -> adminItemService.createBrand(command, user))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.NOT_ALLOW.getMsg());
    }

    @DisplayName("존재하는 브랜드 등록 시 실패한다.")
    @Test
    void createBrand_fail_existsBrand() {
        // given
        User admin =
                createUser("admin@example.com", "qwer123!", "관리자", "01098765432", "남성");
        userRepository.save(admin);
        BrandCommand command = BrandCommand.builder()
                .name("애플")
                .build();
        Brand brand = Brand.builder()
                .name("애플")
                .build();
        brandRepository.save(brand);

        // when // then
        assertThatThrownBy(() -> adminItemService.createBrand(command, admin))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.BRAND_DUPLICATION.getMsg());
    }


    private User createUser(String email, String password, String name, String tel, String gender) {
        return User.builder()
                .email(email)
                .password(password)
                .name(name)
                .tel(tel)
                .gender(gender)
                .role(UserRoleEnum.ADMIN)
                .build();
    }

    private CategoryCommand createCategoryDto(String name, ItemTypeEnum type, Long parentId) {
        return CategoryCommand.builder()
                .name(name)
                .type(type)
                .parentId(parentId)
                .build();
    }

    private Category createCategory(String name, ItemTypeEnum type) {
        return Category.builder()
                .name(name)
                .type(type)
                .build();
    }

}