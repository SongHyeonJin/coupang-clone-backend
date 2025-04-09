package com.example.coupangclone.util;

import com.example.coupangclone.entity.item.Item;
import com.example.coupangclone.enums.ItemTypeEnum;
import com.example.coupangclone.result.ItemResult;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;

public class ItemMapper {

    public static ItemResult toResult(Item item, String imageUrl, double reviewRating, long reviewCnt) {
        NumberFormat numberFormat = NumberFormat.getInstance();

        String discountRate = item.getPrice() - item.getSale() == 0
                ? null
                : numberFormat.format((int) Math.floor((1 - (item.getSale() / (double) item.getPrice())) * 100)) + "%";

        int weight = item.getWeight();
        int totalWeight = weight * item.getSaleCnt();
        String formattedUnitPrice = "", name = "";

        if (weight != 0) {
            if (item.getCategory().getType().equals(ItemTypeEnum.FOOD)) {
                String formattedWeight = formatWeight(weight, "g");
                name = String.format("%s, %s, %d개", item.getName(), formattedWeight, item.getSaleCnt());

                if (totalWeight >= 1000) {
                    formattedUnitPrice = String.format("100g당 %,d원", item.getSale() / (totalWeight / 100));
                } else {
                    formattedUnitPrice = String.format("10g당 %,d원", item.getSale() / (totalWeight / 10));
                }

            } else if (item.getCategory().getType().equals(ItemTypeEnum.LIQUID)) {
                String formattedWeight = formatWeight(weight, "ml");
                name = String.format("%s, %s, %d개", item.getName(), formattedWeight, item.getSaleCnt());

                if (totalWeight >= 1000) {
                    formattedUnitPrice = String.format("100ml당 %,d원", item.getSale() / (totalWeight / 100));
                } else {
                    formattedUnitPrice = String.format("10ml당 %,d원", item.getSale() / (totalWeight / 10));
                }
            }
        } else {
            if (item.getCategory().getType().equals(ItemTypeEnum.THING)) {
                name = String.format("%s, %d개", item.getName(), item.getSaleCnt());
                formattedUnitPrice = String.format("1개당 %,d원", item.getSale());
            }
        }

        LocalDate deliveryDate = LocalDate.now().plusDays(item.getDeliveryTime());
        String dayOfWeek = deliveryDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.KOREAN);
        String delivery = deliveryDate.format(DateTimeFormatter.ofPattern("M/d일")) + "(" + dayOfWeek + ") 도착 예정";

        String deliveryPrice = item.getDeliveryPrice() == 0
                ? "무료배송"
                : "배송비 " + numberFormat.format(item.getDeliveryPrice()) + "원";

        int point = (int) (item.getSale() * 0.01);

        return ItemResult.builder()
                .image(imageUrl)
                .name(name)
                .discountRate(discountRate)
                .price(item.getPrice())
                .sale(item.getSale())
                .unitPrice(formattedUnitPrice)
                .delivery(delivery)
                .deliveryPrice(deliveryPrice)
                .reviewRating(Math.round(reviewRating * 10) / 10.0)
                .reviewCnt(reviewCnt)
                .point(point)
                .build();
    }

    private static String formatWeight(int weight, String unit) {
        if (weight >= 1000) {
            double converted = weight / 1000.0;
            return unit.equals("g") ? String.format("%.1fkg", converted) : String.format("%.1fL", converted);
        }
        return weight + unit;
    }
}
