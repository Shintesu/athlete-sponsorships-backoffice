package com.example.sponsorships.exception;

public class ContractNotFoundException extends RuntimeException {

    public ContractNotFoundException(Long id) {
        super("El contrato solicitado no existe (id " + id + ")");
    }
}
