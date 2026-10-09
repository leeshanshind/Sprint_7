package ru.praktikum.scooter.generator;

import ru.praktikum.scooter.model.Order;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class OrderGenerator {
    private static final String[] FIRST_NAMES = {"Иван", "Мария", "Алексей", "Ольга"};
    private static final String[] LAST_NAMES = {"Иванов", "Петрова", "Смирнов", "Кузнецова"};
    private static final String[] ADDRESSES = {"Москва, ул. Ленина, 1", "Москва, пр. Мира, 15", "Москва, ул. Тверская, 7"};
    private static final String[] COMMENTS = {"Позвоните за час", "Оставьте у двери", "Код домофона 123"};
    private static final int MAX_METRO_STATION = 200;
    private static final int MAX_RENT_DAYS = 7;
    private static final int MAX_DELIVERY_DELAY_DAYS = 14;
    private static final String PHONE_PREFIX = "+7999";
    private static final int PHONE_SUFFIX_BOUND = 10_000_000;

    private static final Random RANDOM = new Random();

    private OrderGenerator() {
    }

    public static Order randomOrder(List<String> color) {
        return new Order(
                pick(FIRST_NAMES),
                pick(LAST_NAMES),
                pick(ADDRESSES),
                RANDOM.nextInt(MAX_METRO_STATION) + 1,
                PHONE_PREFIX + String.format("%07d", RANDOM.nextInt(PHONE_SUFFIX_BOUND)),
                RANDOM.nextInt(MAX_RENT_DAYS) + 1,
                LocalDate.now().plusDays(RANDOM.nextInt(MAX_DELIVERY_DELAY_DAYS) + 1).toString(),
                pick(COMMENTS),
                color
        );
    }

    public static Order randomOrder() {
        return randomOrder(null);
    }

    private static String pick(String[] values) {
        return values[RANDOM.nextInt(values.length)];
    }
}
