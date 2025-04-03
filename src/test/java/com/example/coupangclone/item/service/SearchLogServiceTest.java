package com.example.coupangclone.item.service;

import com.example.coupangclone.item.entity.Brand;
import com.example.coupangclone.item.entity.Item;
import com.example.coupangclone.item.entity.SearchLog;
import com.example.coupangclone.item.repository.BrandRepository;
import com.example.coupangclone.item.repository.ItemRepository;
import com.example.coupangclone.item.repository.SearchLogRepository;
import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.enums.UserRoleEnum;
import com.example.coupangclone.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class SearchLogServiceTest {

    @Autowired
    private SearchLogService searchLogService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private SearchLogRepository searchLogRepository;

    @AfterEach
    void tearDown() {
        searchLogRepository.deleteAllInBatch();
        itemRepository.deleteAllInBatch();
        brandRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("브랜드가 존재하고 상품도 있는 경우 브랜드로 검색 시 로그가 저장된다.")
    @Test
    void saveKeyword_success_existBrand_existItem(){
        // given
        User user = createUser("test@example.com", "qwer123!", "회원", "01034218765", "여성");
        userRepository.save(user);
        Brand brand = createBrand("삼성");
        brandRepository.save(brand);
        Item item1 = createItem("노트북", "정품입니다.", 1600000, 1340000, 1, 1, 0, user, brand);
        Item item2 = createItem("냉장고", "아주 좋습니다.", 2300000, 2030000, 1, 1, 0, user, brand);
        itemRepository.saveAll(List.of(item1, item2));

        String keyword = "삼성";

        // when
        searchLogService.saveKeyword(keyword);

        // then
        List<SearchLog> logs = searchLogRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getMainKeyword()).isEqualTo("삼성");
        assertThat(logs.get(0).getBrand()).isEqualTo("삼성");
    }

    @DisplayName("브랜드가 존재하고 상품도 있는 경우 상품명으로 검색 시 로그가 저장된다.")
    @Test
    void saveKeyword_success_matchingBrand_existItem(){
        // given
        User user = createUser("test@example.com", "qwer123!", "회원", "01034218765", "여성");
        userRepository.save(user);
        Brand brand = createBrand("삼성");
        brandRepository.save(brand);
        Item item1 = createItem("노트북", "정품입니다.", 1600000, 1340000, 1, 1, 0, user, brand);
        Item item2 = createItem("노트북pro", "아주 좋습니다.", 2300000, 2030000, 1, 1, 0, user, brand);
        itemRepository.saveAll(List.of(item1, item2));

        String keyword = "노트북";

        // when
        searchLogService.saveKeyword(keyword);

        // then
        List<SearchLog> logs = searchLogRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getMainKeyword()).isEqualTo("노트북");
        assertThat(logs.get(0).getBrand()).isEqualTo("삼성");
    }

    @DisplayName("브랜드가 없지만 상품이 있으면 상품 로그가 저장된다.")
    @Test
    void saveKeyword_success_no_brand_existItem(){
        // given
        User user = createUser("test@example.com", "qwer123!", "회원", "01034218765", "여성");
        userRepository.save(user);
        Item item1 = createItem("노트북", "정품입니다.", 1600000, 1340000, 1, 1, 0, user, null);
        Item item2 = createItem("노트북pro", "아주 좋습니다.", 2300000, 2030000, 1, 1, 0, user, null);
        itemRepository.saveAll(List.of(item1, item2));

        String keyword = "노트북";

        // when
        searchLogService.saveKeyword(keyword);

        // then
        List<SearchLog> logs = searchLogRepository.findAll();
        assertThat(logs).hasSize(1);
        assertThat(logs.get(0).getMainKeyword()).isEqualTo("노트북");
        assertThat(logs.get(0).getBrand()).isEqualTo(null);
    }

    @DisplayName("키워드가 공백일 경우 아무것도 저장되지 않는다.")
    @Test
    void saveKeyword_blank_thenDoNothing() {
        // when
        searchLogService.saveKeyword(" ");

        // then
        assertThat(searchLogRepository.findAll()).isEmpty();
    }

    @DisplayName("브랜드도 없고 관련 상품도 없으면 저장되지 않는다.")
    @Test
    void saveKeyword_no_brandAndItem_thenDoNothing() {
        // when
        searchLogService.saveKeyword("없는키워드");

        // then
        assertThat(searchLogRepository.findAll()).isEmpty();
    }

    @DisplayName("키워드가 공백이면 빈 리스트를 반환한다.")
    @Test
    void getRelatedKeywordsFor_blank() {
        // when
        List<String> result = searchLogService.getRelatedKeywordsFor(" ");

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("브랜드가 존재하고 관련 상품 키워드 기반으로 연관 키워드를 조회한다.")
    @Test
    void getRelatedKeywordsFor_brandExists() {
        // given
        User user = createUser("test@example.com", "qwer123!", "회원", "01034218765", "여성");
        userRepository.save(user);
        Brand brand = createBrand("삼성");
        brandRepository.save(brand);
        Item item1 = createItem("노트북", "정품입니다.", 1600000, 1340000, 1, 1, 0, user, brand);
        Item item2 = createItem("노트북pro", "아주 좋습니다.", 2300000, 2030000, 1, 1, 0, user, brand);
        Item item3 = createItem("냉장고", "아주 좋습니다.", 2300000, 2030000, 1, 1, 0, user, brand);
        itemRepository.saveAll(List.of(item1, item2, item3));

        SearchLog log1 = SearchLog.builder()
                .mainKeyword("노트북")
                .brand("삼성")
                .keywords(List.of("노트북", "노트북pro"))
                .build();

        SearchLog log2 = SearchLog.builder()
                .mainKeyword("삼성")
                .brand("삼성")
                .keywords(List.of("노트북", "노트북pro", "냉장고"))
                .build();

        SearchLog log3 = SearchLog.builder()
                .mainKeyword("pro")
                .brand("삼성")
                .keywords(List.of("노트북pro"))
                .build();

        searchLogRepository.saveAll(List.of(log1, log2, log3));

        // when
        List<String> result = searchLogService.getRelatedKeywordsFor("노트북");

        // then
        assertThat(result).isNotEmpty();
        assertThat(result).doesNotContain("냉장고");
        assertThat(result).containsAnyOf("노트북pro", "노트북", "냉장고");
    }

    @DisplayName("브랜드가 없고 관련 상품 키워드 기반으로 바로 연관 키워드를 조회한다.")
    @Test
    void getRelatedKeywordsFor_no_brand() {
        // given
        User user = createUser("test@example.com", "qwer123!", "회원", "01034218765", "여성");
        userRepository.save(user);
        Item item1 = createItem("노트북", "정품입니다.", 1600000, 1340000, 1, 1, 0, user, null);
        Item item2 = createItem("노트북pro", "아주 좋습니다.", 2300000, 2030000, 1, 1, 0, user, null);
        itemRepository.saveAll(List.of(item1, item2));

        SearchLog log1 = SearchLog.builder()
                .mainKeyword("노트북")
                .keywords(List.of("노트북", "노트북pro"))
                .build();

        SearchLog log2 = SearchLog.builder()
                .mainKeyword("pro")
                .keywords(List.of("노트북pro"))
                .build();

        searchLogRepository.saveAll(List.of(log1, log2));

        // when
        List<String> result = searchLogService.getRelatedKeywordsFor("노트북");

        // then
        assertThat(result).containsExactlyInAnyOrder("노트북", "노트북pro");
    }

    private User createUser(String email, String password, String name, String tel, String gender) {
        return User.builder()
                .email(email)
                .password(password)
                .name(name)
                .tel(tel)
                .gender(gender)
                .role(UserRoleEnum.USER)
                .build();
    }

    private Brand createBrand(String name) {
        return Brand.builder()
                .name(name)
                .build();
    }

    private Item createItem(String name, String content, int price, int sale, int saleCnt,
                            int deliveryTime, int deliveryPrice, User user, Brand brand) {
        return Item.builder()
                .name(name)
                .content(content)
                .price(price)
                .sale(sale)
                .saleCnt(saleCnt)
                .deliveryTime(deliveryTime)
                .deliveryPrice(deliveryPrice)
                .user(user)
                .brand(brand)
                .build();
    }

}