package com.example.sponsorships.exception;

public class InactiveAthleteException extends RuntimeException {

    public InactiveAthleteException(String fullName) {
        super("No se pueden crear contratos para un atleta inactivo: " + fullName);
    }
}
