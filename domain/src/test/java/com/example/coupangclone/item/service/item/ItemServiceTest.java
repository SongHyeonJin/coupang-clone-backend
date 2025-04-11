package com.example.coupangclone.item.service.item;

import com.example.coupangclone.auth.S3UploadPort;
import com.example.coupangclone.entity.item.*;
import com.example.coupangclone.entity.item.command.ItemCommand;
import com.example.coupangclone.entity.user.User;
import com.example.coupangclone.enums.ItemSortType;
import com.example.coupangclone.enums.ItemTypeEnum;
import com.example.coupangclone.enums.UserRoleEnum;
import com.example.coupangclone.exception.ErrorException;
import com.example.coupangclone.exception.ExceptionEnum;
import com.example.coupangclone.item.support.FakeS3Uploader;
import com.example.coupangclone.result.ItemResult;
import com.example.coupangclone.result.SearchItemResult;
import com.example.coupangclone.service.item.ItemService;
import com.example.coupangclone.service.item.SearchLogService;
import com.example.coupangclone.repository.item.*;
import com.example.coupangclone.repository.user.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Import(ItemServiceTest.TestS3Config.class)
class ItemServiceTest {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemImageRepository itemImageRepository;
    @Autowired
    private SearchLogRepository searchLogRepository;
    @Autowired
    private S3UploadPort s3Uploader;
    @Autowired
    private SearchLogService searchLogService;
    @TestConfiguration
    static class TestS3Config {
        @Bean
        @Primary
        public S3UploadPort s3UploadPort() {
            return new FakeS3Uploader();
        }

    }

    @AfterEach
    void tearDown() {
        searchLogRepository.deleteAllInBatch();
        itemImageRepository.deleteAllInBatch();
        itemRepository.deleteAllInBatch();
        brandRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @DisplayName("회원이 상품 등록에 성공한다.")
    @Test
    void createItem_success() throws IOException {
        // given
        User user =
                createUser("test@example.com", "qwer123!", "김서방", "01043215678", "남성");
        Category category =
                createCategory("전자제품", ItemTypeEnum.THING, null);
        Brand brand = createBrand("삼성");
        userRepository.save(user);
        categoryRepository.save(category);
        brandRepository.save(brand);
        ItemCommand command =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, category.getId(), brand.getId());
        Item item = createItem(command, user, category, brand);

        MockMultipartFile image1 = new MockMultipartFile(
                "images",
                "laptop.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
                "images",
                "samsung.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );
        List<MultipartFile> images = new ArrayList<>(List.of(image1, image2));

        List<ItemImage> itemImages = new ArrayList<>();
        for (MultipartFile image : images) {
            String imageUrl = s3Uploader.upload(image);

            ItemImage itemImage = ItemImage.builder()
                    .image(imageUrl)
                    .item(item)
                    .build();
            itemImages.add(itemImage);
        }

        // when
        itemService.createItem(command, images, user);

        // then
        List<Item> savedItems = itemRepository.findAll();
        assertThat(savedItems).hasSize(1);
        Item savedItem = savedItems.get(0);
        assertThat(savedItem.getName()).isEqualTo("노트북");
        assertThat(savedItem.getPrice()).isEqualTo(1200000);
        assertThat(savedItem.getSale()).isEqualTo(1060000);
        assertThat(savedItem.getUser().getEmail()).isEqualTo("test@example.com");

        List<ItemImage> savedImages = itemImageRepository.findAll();
        assertThat(savedImages).hasSize(2);
        assertThat(savedImages.get(0).getItem().getId()).isEqualTo(savedItem.getId());
    }

    @DisplayName("회원이 아니면 상품 둥록에 실패한다.")
    @Test
    void createItem_fail_not_found_user() throws IOException {
        // given
        User user =
                createUser("test@example.com", "qwer123!", "김서방", "01043215678", "남성");
        Category category =
                createCategory("전자제품", ItemTypeEnum.THING, null);
        Brand brand = createBrand("삼성");
        categoryRepository.save(category);
        brandRepository.save(brand);
        ItemCommand command =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, category.getId(), brand.getId());
        Item item = createItem(command, user, category, brand);

        MockMultipartFile image1 = new MockMultipartFile(
                "images",
                "laptop.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
                "images",
                "samsung.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );
        List<MultipartFile> images = new ArrayList<>(List.of(image1, image2));

        List<ItemImage> itemImages = new ArrayList<>();
        for (MultipartFile image : images) {
            String imageUrl = s3Uploader.upload(image);

            ItemImage itemImage = ItemImage.builder()
                    .image(imageUrl)
                    .item(item)
                    .build();
            itemImages.add(itemImage);
        }

        // when // then
        assertThatThrownBy(() -> itemService.createItem(command, images, user))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.USER_NOT_FOUND.getMsg());
    }

    @DisplayName("카테고리가 존재하지 않으면 상품 둥록에 실패한다.")
    @Test
    void createItem_fail_not_found_category() throws IOException {
        // given
        User user =
                createUser("test@example.com", "qwer123!", "김서방", "01043215678", "남성");
        Category category =
                createCategory("전자제품", ItemTypeEnum.THING, null);
        Brand brand = createBrand("삼성");
        userRepository.save(user);
        categoryRepository.save(category);
        brandRepository.save(brand);
        ItemCommand command =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, 999L, brand.getId());
        Item item = createItem(command, user, category, brand);

        MockMultipartFile image1 = new MockMultipartFile(
                "images",
                "laptop.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
                "images",
                "samsung.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );
        List<MultipartFile> images = new ArrayList<>(List.of(image1, image2));

        List<ItemImage> itemImages = new ArrayList<>();
        for (MultipartFile image : images) {
            String imageUrl = s3Uploader.upload(image);

            ItemImage itemImage = ItemImage.builder()
                    .image(imageUrl)
                    .item(item)
                    .build();
            itemImages.add(itemImage);
        }

        // when // then
        assertThatThrownBy(() -> itemService.createItem(command, images, user))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.CATEGORY_NOT_FOUND.getMsg());
    }

    @DisplayName("브랜드가 존재하지 않으면 상품 둥록에 실패한다.")
    @Test
    void createItem_fail_not_found_brand() throws IOException {
        // given
        User user =
                createUser("test@example.com", "qwer123!", "김서방", "01043215678", "남성");
        Category category =
                createCategory("전자제품", ItemTypeEnum.THING, null);
        Brand brand = createBrand("삼성");
        userRepository.save(user);
        categoryRepository.save(category);
        brandRepository.save(brand);
        ItemCommand command =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, category.getId(), 999L);
        Item item = createItem(command, user, category, brand);

        MockMultipartFile image1 = new MockMultipartFile(
                "images",
                "laptop.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
                "images",
                "samsung.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );
        List<MultipartFile> images = new ArrayList<>(List.of(image1, image2));

        List<ItemImage> itemImages = new ArrayList<>();
        for (MultipartFile image : images) {
            String imageUrl = s3Uploader.upload(image);

            ItemImage itemImage = ItemImage.builder()
                    .image(imageUrl)
                    .item(item)
                    .build();
            itemImages.add(itemImage);
        }

        // when // then
        assertThatThrownBy(() -> itemService.createItem(command, images, user))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.BRAND_NOT_FOUND.getMsg());
    }

    @DisplayName("이미지를 첨부하지 않으면 상품 둥록에 실패한다.")
    @Test
    void createItem_fail_no_image() throws IOException {
        // given
        User user =
                createUser("test@example.com", "qwer123!", "김서방", "01043215678", "남성");
        Category category =
                createCategory("전자제품", ItemTypeEnum.THING, null);
        Brand brand = createBrand("삼성");
        userRepository.save(user);
        categoryRepository.save(category);
        brandRepository.save(brand);
        ItemCommand command =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, category.getId(), brand.getId());
        Item item = createItem(command, user, category, brand);

        List<MultipartFile> images = new ArrayList<>();

        List<ItemImage> itemImages = new ArrayList<>();
        for (MultipartFile image : images) {
            String imageUrl = s3Uploader.upload(image);

            ItemImage itemImage = ItemImage.builder()
                    .image(imageUrl)
                    .item(item)
                    .build();
            itemImages.add(itemImage);
        }

        // when // then
        assertThatThrownBy(() -> itemService.createItem(command, images, user))
                .isInstanceOf(ErrorException.class)
                .hasMessage(ExceptionEnum.IMAGE_REQUIRED.getMsg());
    }

    @DisplayName("상품 목록을 페이징 및 최신 순(기본)으로 조회한다.")
    @Test
    void getItems_success() throws IOException {
        // given
        User user =
                createUser("test@example.com", "qwer123!", "김서방", "01043215678", "남성");
        Category category =
                createCategory("전자제품", ItemTypeEnum.THING, null);
        Brand brand = createBrand("삼성");
        userRepository.save(user);
        categoryRepository.save(category);
        brandRepository.save(brand);
        ItemCommand command1 =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, category.getId(), brand.getId());
        ItemCommand command2 =
                createItemDto("냉장고", 0, "굉장히 좋습니다.", 2100000, 1960000, 1, 1, 0, category.getId(), brand.getId());
        Item item1 = createItem(command1, user, category, brand);
        Item item2 = createItem(command2, user, category, brand);

        MockMultipartFile image1 = new MockMultipartFile(
                "images",
                "laptop.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
                "images",
                "samsung.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );

        List<MultipartFile> images1 = new ArrayList<>(List.of(image1));
        List<ItemImage> itemImages1 = new ArrayList<>();
        for (MultipartFile image : images1) {
            String imageUrl = s3Uploader.upload(image);

            ItemImage itemImage = ItemImage.builder()
                    .image(imageUrl)
                    .item(item1)
                    .build();
            itemImages1.add(itemImage);
        }

        List<ItemImage> itemImages2 = new ArrayList<>();
        List<MultipartFile> images2 = new ArrayList<>(List.of(image2));
        for (MultipartFile image : images2) {
            String imageUrl = s3Uploader.upload(image);

            ItemImage itemImage = ItemImage.builder()
                    .image(imageUrl)
                    .item(item2)
                    .build();
            itemImages2.add(itemImage);
        }
        itemRepository.saveAll(List.of(item1, item2));
        itemImageRepository.saveAll(itemImages1);
        itemImageRepository.saveAll(itemImages2);

        String sort = "";
        ItemSortType sortType = ItemSortType.from(sort);
        Pageable sortedPageable = PageRequest.of(
                0,
                5,
                Sort.by(sortType.getDirection(), sortType.getProperty())
        );

        // when
        Page<ItemResult> result = itemService.getItems(sortedPageable, user, sort);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).name()).contains("냉장고");
        assertThat(result.getContent().get(1).name()).contains("노트북");
    }

    @DisplayName("상품 목록을 페이징 및 높은 가격 순으로 조회한다.")
    @Test
    void getItems_price_desc_success() throws IOException {
        // given
        User user =
                createUser("test@example.com", "qwer123!", "김서방", "01043215678", "남성");
        Category category =
                createCategory("전자제품", ItemTypeEnum.THING, null);
        Brand brand = createBrand("삼성");
        userRepository.save(user);
        categoryRepository.save(category);
        brandRepository.save(brand);
        ItemCommand command1 =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, category.getId(), brand.getId());
        ItemCommand command2 =
                createItemDto("냉장고", 0, "굉장히 좋습니다.", 2100000, 1960000, 1, 1, 0, category.getId(), brand.getId());
        Item item1 = createItem(command1, user, category, brand);
        Item item2 = createItem(command2, user, category, brand);

        MockMultipartFile image1 = new MockMultipartFile(
                "images",
                "laptop.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
                "images",
                "samsung.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );

        List<MultipartFile> images1 = new ArrayList<>(List.of(image1));
        List<ItemImage> itemImages1 = new ArrayList<>();
        for (MultipartFile image : images1) {
            String imageUrl = s3Uploader.upload(image);

            ItemImage itemImage = ItemImage.builder()
                    .image(imageUrl)
                    .item(item1)
                    .build();
            itemImages1.add(itemImage);
        }

        List<ItemImage> itemImages2 = new ArrayList<>();
        List<MultipartFile> images2 = new ArrayList<>(List.of(image2));
        for (MultipartFile image : images2) {
            String imageUrl = s3Uploader.upload(image);

            ItemImage itemImage = ItemImage.builder()
                    .image(imageUrl)
                    .item(item2)
                    .build();
            itemImages2.add(itemImage);
        }
        itemRepository.saveAll(List.of(item1, item2));
        itemImageRepository.saveAll(itemImages1);
        itemImageRepository.saveAll(itemImages2);

        String sort = "price_desc";
        ItemSortType sortType = ItemSortType.from(sort);
        Pageable sortedPageable = PageRequest.of(
                0,
                5,
                Sort.by(sortType.getDirection(), sortType.getProperty())
        );

        // when
        Page<ItemResult> result = itemService.getItems(sortedPageable, user, sort);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).sale()).isEqualTo(1960000);
        assertThat(result.getContent().get(1).sale()).isEqualTo(1060000);
    }

    @DisplayName("상품 목록을 페이징 및 낮음 가격 순으로 조회한다.")
    @Test
    void getItems_price_asc_success() throws IOException {
        // given
        User user =
                createUser("test@example.com", "qwer123!", "김서방", "01043215678", "남성");
        Category category =
                createCategory("전자제품", ItemTypeEnum.THING, null);
        Brand brand = createBrand("삼성");
        userRepository.save(user);
        categoryRepository.save(category);
        brandRepository.save(brand);
        ItemCommand command1 =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, category.getId(), brand.getId());
        ItemCommand command2 =
                createItemDto("냉장고", 0, "굉장히 좋습니다.", 2100000, 1960000, 1, 1, 0, category.getId(), brand.getId());
        Item item1 = createItem(command1, user, category, brand);
        Item item2 = createItem(command2, user, category, brand);

        MockMultipartFile image1 = new MockMultipartFile(
                "images",
                "laptop.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
                "images",
                "samsung.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );

        List<MultipartFile> images1 = new ArrayList<>(List.of(image1));
        List<ItemImage> itemImages1 = new ArrayList<>();
        for (MultipartFile image : images1) {
            String imageUrl = s3Uploader.upload(image);

            ItemImage itemImage = ItemImage.builder()
                    .image(imageUrl)
                    .item(item1)
                    .build();
            itemImages1.add(itemImage);
        }

        List<ItemImage> itemImages2 = new ArrayList<>();
        List<MultipartFile> images2 = new ArrayList<>(List.of(image2));
        for (MultipartFile image : images2) {
            String imageUrl = s3Uploader.upload(image);

            ItemImage itemImage = ItemImage.builder()
                    .image(imageUrl)
                    .item(item2)
                    .build();
            itemImages2.add(itemImage);
        }
        itemRepository.saveAll(List.of(item1, item2));
        itemImageRepository.saveAll(itemImages1);
        itemImageRepository.saveAll(itemImages2);

        String sort = "price_asc";
        ItemSortType sortType = ItemSortType.from(sort);
        Pageable sortedPageable = PageRequest.of(
                0,
                5,
                Sort.by(sortType.getDirection(), sortType.getProperty())
        );

        // when
        Page<ItemResult> result = itemService.getItems(sortedPageable, user, sort);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).sale()).isEqualTo(1060000);
        assertThat(result.getContent().get(1).sale()).isEqualTo(1960000);
    }

    @DisplayName("키워드의 연관검색어와 상품 검색에 성공한다.")
    @Test
    void searchItems_success() throws IOException {
        User user =
                createUser("test@example.com", "qwer123!", "김서방", "01043215678", "남성");
        Category category =
                createCategory("전자제품", ItemTypeEnum.THING, null);
        Brand brand = createBrand("삼성");
        userRepository.save(user);
        categoryRepository.save(category);
        brandRepository.save(brand);
        ItemCommand command1 =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, category.getId(), brand.getId());
        ItemCommand command2 =
                createItemDto("냉장고", 0, "굉장히 좋습니다.", 2100000, 1960000, 1, 1, 0, category.getId(), brand.getId());
        Item item1 = createItem(command1, user, category, brand);
        Item item2 = createItem(command2, user, category, brand);

        MockMultipartFile image1 = new MockMultipartFile(
                "images",
                "laptop.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
                "images",
                "samsung.jpg",
                "image/jpg",
                "fake image content".getBytes()
        );

        List<MultipartFile> images1 = new ArrayList<>(List.of(image1));
        List<ItemImage> itemImages1 = new ArrayList<>();
        for (MultipartFile image : images1) {
            String imageUrl = s3Uploader.upload(image);

            ItemImage itemImage = ItemImage.builder()
                    .image(imageUrl)
                    .item(item1)
                    .build();
            itemImages1.add(itemImage);
        }

        List<ItemImage> itemImages2 = new ArrayList<>();
        List<MultipartFile> images2 = new ArrayList<>(List.of(image2));
        for (MultipartFile image : images2) {
            String imageUrl = s3Uploader.upload(image);

            ItemImage itemImage = ItemImage.builder()
                    .image(imageUrl)
                    .item(item2)
                    .build();
            itemImages2.add(itemImage);
        }
        itemRepository.saveAll(List.of(item1, item2));
        itemImageRepository.saveAll(itemImages1);
        itemImageRepository.saveAll(itemImages2);

        String keyword = "노트북";
        Pageable sortedPageable = PageRequest.of(0, 5);

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
        SearchItemResult result = itemService.searchItems(keyword, sortedPageable, user);

        // then
        assertThat(result.items()).isNotEmpty();
        assertThat(result.items().getContent()).hasSize(1);
        assertThat(result.items().getContent().get(0).name()).contains("노트북");
        assertThat(result.relatedKeywords()).containsExactlyInAnyOrder("노트북", "노트북pro");
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

    private Category createCategory(String name, ItemTypeEnum type, Category parent) {
        return Category.builder()
                .name(name)
                .type(type)
                .parent(parent)
                .build();
    }

    private Brand createBrand(String name) {
        return Brand.builder()
                .name(name)
                .build();
    }

    private Item createItem(ItemCommand command, User user, Category category, Brand brand) {
        return Item.builder()
                .name(command.name())
                .weight(command.weight())
                .content(command.content())
                .price(command.price())
                .sale(command.sale())
                .saleCnt(command.saleCnt())
                .deliveryTime(command.deliveryTime())
                .deliveryPrice(command.deliveryPrice())
                .user(user)
                .category(category)
                .brand(brand)
                .build();
    }

    private ItemCommand createItemDto(String name, int weight, String content, int price, int sale, int saleCnt,
                                         int deliveryTime, int deliveryPrice, Long categoryId, Long brandId) {
        return ItemCommand.builder()
                .name(name)
                .weight(weight)
                .content(content)
                .price(price)
                .sale(sale)
                .saleCnt(saleCnt)
                .deliveryTime(deliveryTime)
                .deliveryPrice(deliveryPrice)
                .categoryId(categoryId)
                .brandId(brandId)
                .build();
    }

}