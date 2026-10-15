package com.example.sponsorships.exception;

public class OverlappingContractException extends RuntimeException {

    public OverlappingContractException() {
        super("El contrato se solapa con otro contrato del atleta que aún no ha vencido");
    }
}
