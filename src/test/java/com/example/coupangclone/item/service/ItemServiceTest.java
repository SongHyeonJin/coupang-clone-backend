package com.example.coupangclone.item.service;

import com.example.coupangclone.config.S3Uploader;
import com.example.coupangclone.global.dto.BasicResponseDto;
import com.example.coupangclone.global.exception.ErrorException;
import com.example.coupangclone.global.exception.ExceptionEnum;
import com.example.coupangclone.item.dto.item.ItemRequestDto;
import com.example.coupangclone.item.dto.item.ItemResponseDto;
import com.example.coupangclone.item.entity.Brand;
import com.example.coupangclone.item.entity.Category;
import com.example.coupangclone.item.entity.Item;
import com.example.coupangclone.item.entity.ItemImage;
import com.example.coupangclone.item.enums.ItemSortType;
import com.example.coupangclone.item.enums.ItemTypeEnum;
import com.example.coupangclone.item.repository.BrandRepository;
import com.example.coupangclone.item.repository.CategoryRepository;
import com.example.coupangclone.item.repository.ItemImageRepository;
import com.example.coupangclone.item.repository.ItemRepository;
import com.example.coupangclone.user.entity.User;
import com.example.coupangclone.user.enums.UserRoleEnum;
import com.example.coupangclone.user.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
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
    private S3Uploader s3Uploader;

    @AfterEach
    void tearDown() {
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
        ItemRequestDto requestDto =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, category.getId(), brand.getId());
        Item item = createItem(requestDto, user, category, brand);

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
        ResponseEntity<?> response = itemService.createItem(requestDto, images, user);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(BasicResponseDto.class);

        BasicResponseDto responseDto = (BasicResponseDto) response.getBody();
        assertThat(responseDto.getMsg()).isEqualTo("상품 등록이 완료되었습니다.");
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
        ItemRequestDto requestDto =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, category.getId(), brand.getId());
        Item item = createItem(requestDto, user, category, brand);

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
        assertThatThrownBy(() -> itemService.createItem(requestDto, images, user))
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
        ItemRequestDto requestDto =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, 999L, brand.getId());
        Item item = createItem(requestDto, user, category, brand);

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
        assertThatThrownBy(() -> itemService.createItem(requestDto, images, user))
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
        ItemRequestDto requestDto =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, category.getId(), 999L);
        Item item = createItem(requestDto, user, category, brand);

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
        assertThatThrownBy(() -> itemService.createItem(requestDto, images, user))
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
        ItemRequestDto requestDto =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, category.getId(), brand.getId());
        Item item = createItem(requestDto, user, category, brand);

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
        assertThatThrownBy(() -> itemService.createItem(requestDto, images, user))
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
        ItemRequestDto requestDto1 =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, category.getId(), brand.getId());
        ItemRequestDto requestDto2 =
                createItemDto("냉장고", 0, "굉장히 좋습니다.", 2100000, 1960000, 1, 1, 0, category.getId(), brand.getId());
        Item item1 = createItem(requestDto1, user, category, brand);
        Item item2 = createItem(requestDto2, user, category, brand);

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
        ResponseEntity<Page<ItemResponseDto>> response = itemService.getItems(sortedPageable, user, sort);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        Page<ItemResponseDto> itemResponseDtos = response.getBody();
        assertThat(itemResponseDtos.getContent()).hasSize(2);

        ItemResponseDto firstItem = itemResponseDtos.getContent().get(0);
        assertThat(firstItem.getName()).isEqualTo("냉장고, 1개");
        assertThat(firstItem.getSale()).isEqualTo(1960000);

        ItemResponseDto secondItem = itemResponseDtos.getContent().get(1);
        assertThat(secondItem.getName()).isEqualTo("노트북, 1개");
        assertThat(secondItem.getSale()).isEqualTo(1060000);
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
        ItemRequestDto requestDto1 =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, category.getId(), brand.getId());
        ItemRequestDto requestDto2 =
                createItemDto("냉장고", 0, "굉장히 좋습니다.", 2100000, 1960000, 1, 1, 0, category.getId(), brand.getId());
        Item item1 = createItem(requestDto1, user, category, brand);
        Item item2 = createItem(requestDto2, user, category, brand);

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
        ResponseEntity<Page<ItemResponseDto>> response = itemService.getItems(sortedPageable, user, sort);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        Page<ItemResponseDto> itemResponseDtos = response.getBody();
        assertThat(itemResponseDtos.getContent()).hasSize(2);

        ItemResponseDto firstItem = itemResponseDtos.getContent().get(0);
        assertThat(firstItem.getName()).isEqualTo("냉장고, 1개");
        assertThat(firstItem.getSale()).isEqualTo(1960000);

        ItemResponseDto secondItem = itemResponseDtos.getContent().get(1);
        assertThat(secondItem.getName()).isEqualTo("노트북, 1개");
        assertThat(secondItem.getSale()).isEqualTo(1060000);
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
        ItemRequestDto requestDto1 =
                createItemDto("노트북", 0, "정품입니다.", 1200000, 1060000, 1, 1, 0, category.getId(), brand.getId());
        ItemRequestDto requestDto2 =
                createItemDto("냉장고", 0, "굉장히 좋습니다.", 2100000, 1960000, 1, 1, 0, category.getId(), brand.getId());
        Item item1 = createItem(requestDto1, user, category, brand);
        Item item2 = createItem(requestDto2, user, category, brand);

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
        ResponseEntity<Page<ItemResponseDto>> response = itemService.getItems(sortedPageable, user, sort);

        // then
        assertThat(response.getStatusCode().value()).isEqualTo(200);

        Page<ItemResponseDto> itemResponseDtos = response.getBody();
        assertThat(itemResponseDtos.getContent()).hasSize(2);

        ItemResponseDto firstItem = itemResponseDtos.getContent().get(0);
        assertThat(firstItem.getName()).isEqualTo("노트북, 1개");
        assertThat(firstItem.getSale()).isEqualTo(1060000);

        ItemResponseDto secondItem = itemResponseDtos.getContent().get(1);
        assertThat(secondItem.getName()).isEqualTo("냉장고, 1개");
        assertThat(secondItem.getSale()).isEqualTo(1960000);
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

    private Item createItem(ItemRequestDto requestDto, User user, Category category, Brand brand) {
        return Item.builder()
                .name(requestDto.getName())
                .weight(requestDto.getWeight())
                .content(requestDto.getContent())
                .price(requestDto.getPrice())
                .sale(requestDto.getSale())
                .saleCnt(requestDto.getSaleCnt())
                .deliveryTime(requestDto.getDeliveryTime())
                .deliveryPrice(requestDto.getDeliveryPrice())
                .user(user)
                .category(category)
                .brand(brand)
                .build();
    }

    private ItemRequestDto createItemDto(String name, int weight, String content, int price, int sale, int saleCnt,
                                         int deliveryTime, int deliveryPrice, Long categoryId, Long brandId) {
        return ItemRequestDto.builder()
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