package ru.praktikum.scooter.generator;

import ru.praktikum.scooter.model.Courier;

import java.util.UUID;

public class CourierGenerator {
    private static final String LOGIN_PREFIX = "courier_";
    private static final int RANDOM_PART_LENGTH = 10;

    private CourierGenerator() {
    }

    public static Courier randomCourier() {
        return new Courier(LOGIN_PREFIX + randomString(), randomString(), randomString());
    }

    public static String randomString() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, RANDOM_PART_LENGTH);
    }
}
