package com.example.sponsorships.enums;

import lombok.Getter;

@Getter
public enum ContractStatus {

    DRAFT("Borrador"),
    IN_FORCE("Vigente"),
    EXPIRING("Por vencer"),
    EXPIRED("Vencido");

    private final String label;

    ContractStatus(String label) {
        this.label = label;
    }
}
