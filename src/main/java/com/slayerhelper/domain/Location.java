package com.slayerhelper.domain;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Location {
    private final String name;
    private final boolean cannonable;
    private final boolean multicombat;

    public Location(String name, boolean cannonable, boolean multicombat) {
        this.name = Objects.requireNonNull(name, "location name cannot be null");
        this.cannonable = cannonable;
        this.multicombat = multicombat;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
