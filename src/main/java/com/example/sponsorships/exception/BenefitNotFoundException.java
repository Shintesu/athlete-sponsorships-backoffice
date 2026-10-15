package com.example.sponsorships.exception;

public class BenefitNotFoundException extends RuntimeException {

    public BenefitNotFoundException(Long id) {
        super("El beneficio solicitado no existe (id " + id + ")");
    }
}
