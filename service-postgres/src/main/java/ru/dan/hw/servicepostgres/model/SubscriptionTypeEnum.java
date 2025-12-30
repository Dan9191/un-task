package ru.dan.hw.servicepostgres.model;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum SubscriptionTypeEnum {
    BASIC(1, "BASIC", 100),
    PRO(2, "PRO", 200);

    private final int id;
    private final String name;
    private final int price;

    SubscriptionTypeEnum(int id, String name, int price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    public static SubscriptionTypeEnum fromId(int id) {
        return Arrays.stream(values())
                .filter(type -> type.id == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Неизвестный ID: " + id));
    }
}
