package com.example.coupangclone.item.dto.item;

import com.example.coupangclone.item.entity.Item;
import com.example.coupangclone.item.enums.ItemTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

@Getter
@Builder
@Schema(description = "전체 상품 응답 DTO")
public class ItemResponseDto {
    // 대표 이미지
    private String image;
    private String name;
    // 할인율
    private String discountRate;
    // 실제가격
    private int price;
    //판매가격
    private int sale;
    // 3개면 1개당 가격, 500g이면 100g당 가격
    private String unitPrice;
    // 예샹배달도착임
    private String delivery;
    // 예상배달가격 0원일 시 무료라고 찍혀야됨
    private String deliveryPrice;
    // 리뷰 점수 (총 점수/리뷰 수)
    private double reviewRating;
    // 리뷰 수
    private long reviewCnt;
    // 판매가의 1% 적립
    private int point;

    public static ItemResponseDto of(Item item, String imageUrl, double reviewRating, long reviewCnt) {
        NumberFormat numberFormat = NumberFormat.getInstance();

        // 할인율 계산
        String discountRate = item.getPrice() - item.getSale() == 0
                ? null
                : numberFormat.format((int) Math.floor((1 - (item.getSale() / (double) item.getPrice())) * 100)) + "%";

        // 단위 가격 및 이름 포맷팅
        int unitPrice;
        int weight = item.getWeight();
        String formattedUnitPrice = "", name = "";
        int totalWeight = weight * item.getSaleCnt();

        if (weight != 0) {
            if (item.getCategory().getType().equals(ItemTypeEnum.FOOD)) {
                String formattedWeight = formatWeight(weight, "g");
                name = String.format("%s, %s, %d개", item.getName(), formattedWeight, item.getSaleCnt());

                if (totalWeight >= 1000) {
                    unitPrice = item.getSale() / (totalWeight / 100);
                    formattedUnitPrice = String.format("100g당 %,d원", unitPrice);
                } else {
                    unitPrice = item.getSale() / (totalWeight / 10);
                    formattedUnitPrice = String.format("10g당 %,d원", unitPrice);
                }

            } else if (item.getCategory().getType().equals(ItemTypeEnum.LIQUID)) {
                String formattedWeight = formatWeight(weight, "ml");
                name = String.format("%s, %s, %d개", item.getName(), formattedWeight, item.getSaleCnt());

                if (totalWeight >= 1000) {
                    unitPrice = item.getSale() / (totalWeight / 100);
                    formattedUnitPrice = String.format("100ml당 %,d원", unitPrice);
                } else {
                    unitPrice = item.getSale() / (totalWeight / 10);
                    formattedUnitPrice = String.format("10ml당 %,d원", unitPrice);
                }
            }
        } else {
            if (item.getCategory().getType().equals(ItemTypeEnum.THING)) {
                name = String.format("%s, %d개", item.getName(), item.getSaleCnt());
                unitPrice = item.getSale();
                formattedUnitPrice = String.format("1개당 %,d원", unitPrice);
            }
        }

        LocalDate deliveryDate = LocalDate.now().plusDays(item.getDeliveryTime());
        String dayOfWeek = deliveryDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
        String formattedDate = deliveryDate.format(DateTimeFormatter.ofPattern("M/d일"));
        String delivery = formattedDate + "(" + dayOfWeek + ") 도착 예정";

        String deliveryPrice = item.getDeliveryPrice() == 0
                ? "무료배송"
                : "배송비 " + numberFormat.format(item.getDeliveryPrice()) + "원";

        int point = (int) (item.getSale() * 0.01);

        return ItemResponseDto.builder()
                .image(imageUrl)
                .name(name)
                .discountRate(discountRate)
                .price(item.getPrice())
                .sale(item.getSale())
                .unitPrice(formattedUnitPrice)
                .delivery(delivery)
                .deliveryPrice(deliveryPrice)
                .reviewCnt(reviewCnt)
                .reviewRating(Math.round(reviewRating * 10) / 10.0)
                .point(point)
                .build();
    }

    private static String formatWeight(int weight, String unit) {
        if (weight >= 1000) {
            double converted = weight / 1000.0;
            if (unit.equals("g")) return String.format("%.1fkg", converted);
            if (unit.equals("ml")) return String.format("%.1fL", converted);
        }
        return weight + unit;
    }

}
