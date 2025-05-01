package com.slayerhelper.domain;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Item {
    private final String name;
    private final String icon;

    public Item(String name, String icon) {
        this.name = Objects.requireNonNull(name, "item name cannot be null");
        this.icon = icon;
    }

}