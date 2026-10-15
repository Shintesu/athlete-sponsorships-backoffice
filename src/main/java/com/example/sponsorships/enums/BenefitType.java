package com.example.sponsorships.enums;

import lombok.Getter;

@Getter
public enum BenefitType {

    EQUIPMENT("Equipo"),
    PAYMENT("Pago"),
    EVENT("Evento");

    private final String label;

    BenefitType(String label) {
        this.label = label;
    }
}
