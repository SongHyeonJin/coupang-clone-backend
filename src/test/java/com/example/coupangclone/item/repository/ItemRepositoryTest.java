package com.example.coupangclone.item.repository;

import com.example.coupangclone.item.entity.Brand;
import com.example.coupangclone.item.entity.Item;
import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.enums.UserRoleEnum;
import com.example.coupangclone.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private ItemRepository itemRepository;

    @DisplayName("브랜드가 존재하면 브랜드로 검색 시 해당 브랜드 상품들이 조회된다.")
    @Test
    void searchByNameOrBrand_searchBrand_brandExists(){
        // given
        User user = createUser("test@example.com", "qwer123!", "회원", "01034218765", "여성");
        userRepository.save(user);
        Brand brand = createBrand("삼성");
        brandRepository.save(brand);
        Item item = createItem("노트북", "정품입니다.", 1600000, 1340000, 1, 1, 0, user, brand);
        itemRepository.save(item);

        String keyword = "삼성";
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<Item> result = itemRepository.searchByNameOrBrand(keyword, pageable);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getName()).isEqualTo("노트북");
        assertThat(result.getContent().get(0).getBrand().getName()).isEqualTo("삼성");
    }

    @DisplayName("브랜드가 존재하는 상품 검색 시 해당 상품들이 조회된다.")
    @Test
    void searchByNameOrBrand_searchItem_brandExists(){
        // given
        User user = createUser("test@example.com", "qwer123!", "회원", "01034218765", "여성");
        userRepository.save(user);
        Brand brand = createBrand("삼성");
        brandRepository.save(brand);
        Item item = createItem("노트북pro", "정품입니다.", 1600000, 1340000, 1, 1, 0, user, brand);
        itemRepository.save(item);

        String keyword = "노트북";
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<Item> result = itemRepository.searchByNameOrBrand(keyword, pageable);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getName()).isEqualTo("노트북pro");
        assertThat(result.getContent().get(0).getBrand().getName()).isEqualTo("삼성");
    }

    @DisplayName("브랜드가 존재하지 않는 상품 검색 시 해당 상품들이 조회된다.")
    @Test
    void searchByNameOrBrand_no_brand(){
        // given
        User user = createUser("test@example.com", "qwer123!", "회원", "01034218765", "여성");
        userRepository.save(user);
        Item item = createItem("노트북pro", "정품입니다.", 1600000, 1340000, 1, 1, 0, user, null);
        itemRepository.save(item);

        String keyword = "노트북";
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<Item> result = itemRepository.searchByNameOrBrand(keyword, pageable);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getName()).isEqualTo("노트북pro");
        assertThat(result.getContent().get(0).getBrand()).isNull();
    }

    @DisplayName("상품 검색 시 알파벳 대소문자 구분 없이 상품명이 조회된다.")
    @Test
    void searchByNameOrBrand_ignoreCase_searchItem(){
        // given
        User user = createUser("test@example.com", "qwer123!", "회원", "01034218765", "여성");
        userRepository.save(user);
        Brand brand = createBrand("삼성");
        brandRepository.save(brand);
        Item item = createItem("노트북pro", "정품입니다.", 1600000, 1340000, 1, 1, 0, user, brand);
        itemRepository.save(item);

        String keyword = "노트북PRO";
        Pageable pageable = PageRequest.of(0, 5);

        // when
        Page<Item> result = itemRepository.searchByNameOrBrand(keyword, pageable);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.getContent().get(0).getName()).isEqualTo("노트북pro");
        assertThat(result.getContent().get(0).getBrand().getName()).isEqualTo("삼성");
    }

    @DisplayName("상품 검색을 상품명으로 할 시  상품명이 조회된다.")
    @Test
    void findMatchingItemsByNameOrBrand_searchItem(){
        // given
        User user = createUser("test@example.com", "qwer123!", "회원", "01034218765", "여성");
        userRepository.save(user);
        Brand brand = createBrand("삼성");
        brandRepository.save(brand);
        Item item = createItem("노트북", "정품입니다.", 1600000, 1340000, 1, 1, 0, user, brand);
        itemRepository.save(item);

        String keyword = "노트북";
        // when
        List<Item> result = itemRepository.findMatchingItemsByNameOrBrand(keyword);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo("노트북");
        assertThat(result.get(0).getBrand().getName()).isEqualTo("삼성");
    }

    @DisplayName("브랜드가 존재하는 상품 검색을 할 시 해당 브랜드 상품명이 조회된다.")
    @Test
    void findMatchingItemsByNameOrBrand_searchItem_brandExists(){
        // given
        User user = createUser("test@example.com", "qwer123!", "회원", "01034218765", "여성");
        userRepository.save(user);
        Brand brand = createBrand("삼성");
        brandRepository.save(brand);
        Item item = createItem("노트북", "정품입니다.", 1600000, 1340000, 1, 1, 0, user, brand);
        itemRepository.save(item);

        String keyword = "삼성";
        // when
        List<Item> result = itemRepository.findMatchingItemsByNameOrBrand(keyword);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo("노트북");
        assertThat(result.get(0).getBrand().getName()).isEqualTo("삼성");
    }

    @DisplayName("브랜드가 존재하지 않는 상품 검색을 할 시 해당 브랜드 상품명이 조회되지 않는다.")
    @Test
    void findMatchingItemsByNameOrBrand_searchItem_no_brand(){
        // given
        User user = createUser("test@example.com", "qwer123!", "회원", "01034218765", "여성");
        userRepository.save(user);
        Item item = createItem("사과", "맛있어요.", 1600000, 1340000, 1, 1, 0, user, null);
        itemRepository.save(item);

        String keyword = "삼성";
        // when
        List<Item> result = itemRepository.findMatchingItemsByNameOrBrand(keyword);

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("상품 검색 시 알파벳 대소문자 구분 없이 상품명이 조회된다.")
    @Test
    void findMatchingItemsByNameOrBrand_ignoreCase_searchItem(){
        // given
        User user = createUser("test@example.com", "qwer123!", "회원", "01034218765", "여성");
        userRepository.save(user);
        Brand brand = createBrand("삼성");
        brandRepository.save(brand);
        Item item = createItem("노트북pro", "정품입니다.", 1600000, 1340000, 1, 1, 0, user, brand);
        itemRepository.save(item);

        String keyword = "노트북PRO";
        // when
        List<Item> result = itemRepository.findMatchingItemsByNameOrBrand(keyword);

        // then
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getName()).isEqualTo("노트북pro");
        assertThat(result.get(0).getBrand().getName()).isEqualTo("삼성");
    }

    @DisplayName("브랜드로 상품을 조회하면 해당 브랜드의 상위 5개 상품이 반환된다.")
    @Test
    void findTop5ByBrand_searchItem(){
        // given
        User user = createUser("test@example.com", "qwer123!", "회원", "01034218765", "여성");
        userRepository.save(user);
        Brand brand = createBrand("삼성");
        brandRepository.save(brand);
        Item item1 = createItem("노트북", "정품입니다.", 1600000, 1340000, 1, 1, 0, user, brand);
        Item item2 = createItem("노트북pro", "정품입니다.", 2300000, 2140000, 1, 1, 0, user, brand);
        Item item3 = createItem("갤럭시24", "정품입니다.", 1500000, 1210000, 1, 1, 0, user, brand);
        Item item4 = createItem("갤럭시 워치", "정품입니다.", 600000, 540000, 1, 1, 0, user, brand);
        Item item5 = createItem("갤럭시 버즈", "정품입니다.", 310000, 280000, 1, 1, 0, user, brand);
        Item item6 = createItem("냉장고", "정품입니다.", 2600000, 2420000, 1, 1, 0, user, brand);
        Item item7 = createItem("노트북", "정품입니다.", 1600000, 1320000, 1, 1, 0, user, brand);
        itemRepository.saveAll(List.of(item1, item2, item3, item4, item5, item6, item7));

        // when
        List<Item> result = itemRepository.findTop5ByBrand(brand);

        // then
        assertThat(result).hasSize(5);

        List<String> itemNames = result.stream()
                .map(Item::getName)
                .toList();

        assertThat(itemNames).containsExactlyInAnyOrder(
                "노트북", "노트북pro", "갤럭시24", "갤럭시 워치", "갤럭시 버즈"
        );
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