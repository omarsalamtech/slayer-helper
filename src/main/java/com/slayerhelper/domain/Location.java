package com.slayerhelper.domain;

import lombok.Getter;

import java.util.Objects;

@Getter
public class Location {
    private final String name;
    private final boolean cannonable;
    
    public Location(String name, boolean cannonable) {
        this.name = Objects.requireNonNull(name, "location name cannot be null");
        this.cannonable = cannonable;
    }
    
    @Override
    public String toString() {
        return name;
    }
}
