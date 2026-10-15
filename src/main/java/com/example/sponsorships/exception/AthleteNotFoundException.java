package com.example.sponsorships.exception;

public class AthleteNotFoundException extends RuntimeException {

    public AthleteNotFoundException(Long id) {
        super("El atleta solicitado no existe (id " + id + ")");
    }
}
