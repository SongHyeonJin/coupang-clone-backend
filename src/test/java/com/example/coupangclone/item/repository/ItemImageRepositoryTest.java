package com.example.coupangclone.item.repository;

import com.example.coupangclone.item.entity.Item;
import com.example.coupangclone.item.entity.ItemImage;
import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.enums.UserRoleEnum;
import com.example.coupangclone.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ItemImageRepositoryTest {

    @Autowired
    private ItemImageRepository itemImageRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private UserRepository userRepository;

    @DisplayName("상품이 있을 시 대표 이미지 하나를 가져온다.")
    @Test
    void findFirstByItemId(){
        // given
        User user = createUser("test@example.com", "qwer123!", "회원", "01034218765", "여성");
        userRepository.save(user);
        Item item = createItem("노트북", "정품입니다.", 1600000, 1340000, 1, 1, 0, user);
        itemRepository.save(item);
        ItemImage itemImage1 = createItemImage("image1.jpg", item);
        ItemImage itemImage2 = createItemImage("image2.jpg", item);
        itemImageRepository.saveAll(List.of(itemImage1, itemImage2));

        // when
        Optional<ItemImage> result = itemImageRepository.findFirstByItemId(item.getId());

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getItem().getId()).isEqualTo(item.getId());
        assertThat(result.get().getImage()).isEqualTo("image1.jpg");
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

    private Item createItem(String name, String content, int price, int sale, int saleCnt,
                            int deliveryTime, int deliveryPrice, User user) {
        return Item.builder()
                .name(name)
                .content(content)
                .price(price)
                .sale(sale)
                .saleCnt(saleCnt)
                .deliveryTime(deliveryTime)
                .deliveryPrice(deliveryPrice)
                .user(user)
                .build();
    }

    private ItemImage createItemImage(String image, Item item) {
        return ItemImage.builder()
                .image(image)
                .item(item)
                .build();
    }

}