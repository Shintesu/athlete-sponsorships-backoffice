package com.example.sponsorships.exception;

public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException() {
        super("La fecha de finalización no puede ser anterior a la fecha de inicio");
    }
}
