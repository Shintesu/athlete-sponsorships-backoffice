package com.example.sponsorships.controller;

import com.example.sponsorships.exception.AthleteNotFoundException;
import com.example.sponsorships.exception.BenefitNotFoundException;
import com.example.sponsorships.exception.ContractNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({AthleteNotFoundException.class, ContractNotFoundException.class, BenefitNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(RuntimeException exception, Model model) {
        model.addAttribute("message", exception.getMessage());
        return "error";
    }
}
