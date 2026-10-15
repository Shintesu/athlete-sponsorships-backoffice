package com.example.sponsorships.exception;

public class BenefitInUseException extends RuntimeException {

    public BenefitInUseException(String name) {
        super("No se puede eliminar el beneficio porque está asociado a uno o más contratos: " + name);
    }
}
