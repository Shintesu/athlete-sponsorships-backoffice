package com.example.sponsorships.enums;

import lombok.Getter;

@Getter
public enum AthleteStatus {

    ACTIVE("Activo"),
    INACTIVE("Inactivo");

    private final String label;

    AthleteStatus(String label) {
        this.label = label;
    }
}
