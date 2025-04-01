package com.example.coupangclone.item.service;

import com.example.coupangclone.global.dto.BasicResponseDto;
import com.example.coupangclone.global.exception.ErrorException;
import com.example.coupangclone.global.exception.ExceptionEnum;
import com.example.coupangclone.item.dto.brand.BrandRequestDto;
import com.example.coupangclone.item.dto.category.CategoryRequestDto;
import com.example.coupangclone.item.entity.Brand;
import com.example.coupangclone.item.entity.Category;
import com.example.coupangclone.item.enums.ItemTypeEnum;
import com.example.coupangclone.item.repository.BrandRepository;
import com.example.coupangclone.item.repository.CategoryRepository;
import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.enums.UserRoleEnum;
import com.example.coupangclone.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
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
        CategoryRequestDto requestDto = createCategoryDto("전자제품", ItemTypeEnum.THING, null);

        // when
        ResponseEntity<?> response = adminItemService.createCategory(requestDto, admin);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(BasicResponseDto.class);

        BasicResponseDto responseDto = (BasicResponseDto) response.getBody();
        assertThat(responseDto.getMsg()).isEqualTo("카테고리 등록이 완료되었습니다.");
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
        CategoryRequestDto requestDto = createCategoryDto("전자제품", ItemTypeEnum.THING, null);

        // when // then
        assertThatThrownBy(() -> adminItemService.createCategory(requestDto, user))
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
        CategoryRequestDto requestDto = createCategoryDto("전자제품", ItemTypeEnum.THING, null);
        Category category = createCategory(requestDto);
        categoryRepository.save(category);

        // when // then
        assertThatThrownBy(() -> adminItemService.createCategory(requestDto, admin))
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
        Category category = Category.builder()
                .name("과일")
                .type(ItemTypeEnum.FOOD)
                .build();
        categoryRepository.save(category);
        categoryRepository.deleteById(category.getId());
        CategoryRequestDto requestDto = createCategoryDto("사과", ItemTypeEnum.FOOD, category);

        // when // then
        assertThatThrownBy(() -> adminItemService.createCategory(requestDto, admin))
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
        BrandRequestDto requestDto = BrandRequestDto.builder()
                .name("애플")
                .build();

        // when
        ResponseEntity<?> response = adminItemService.createBrand(requestDto, admin);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(BasicResponseDto.class);

        BasicResponseDto responseDto = (BasicResponseDto) response.getBody();
        assertThat(responseDto.getMsg()).isEqualTo("브랜드 등록이 완료되었습니다.");
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
        BrandRequestDto requestDto = BrandRequestDto.builder()
                .name("애플")
                .build();

        // when // then
        assertThatThrownBy(() -> adminItemService.createBrand(requestDto, user))
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
        BrandRequestDto requestDto = BrandRequestDto.builder()
                .name("애플")
                .build();
        Brand brand = Brand.builder()
                .name("애플")
                .build();
        brandRepository.save(brand);

        // when // then
        assertThatThrownBy(() -> adminItemService.createBrand(requestDto, admin))
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

    private CategoryRequestDto createCategoryDto(String name, ItemTypeEnum type, Category parent) {
        return CategoryRequestDto.builder()
                .name(name)
                .type(type)
                .parent(parent)
                .build();
    }

    private Category createCategory(CategoryRequestDto requestDto) {
        return Category.builder()
                .name(requestDto.getName())
                .type(requestDto.getType())
                .parent(requestDto.getParent())
                .build();
    }

}