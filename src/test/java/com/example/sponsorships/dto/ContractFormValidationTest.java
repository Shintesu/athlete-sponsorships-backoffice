package com.example.sponsorships.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ContractFormValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        validatorFactory.close();
    }

    @Test
    void rejectsMissingDates() {
        ContractForm form = new ContractForm();

        Set<ConstraintViolation<ContractForm>> violations = validator.validate(form);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("startDate", "endDate");
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        ContractForm form = new ContractForm();
        form.setStartDate(LocalDate.of(2026, 6, 1));
        form.setEndDate(LocalDate.of(2026, 5, 1));

        Set<ConstraintViolation<ContractForm>> violations = validator.validate(form);

        assertThat(violations)
                .extracting(ConstraintViolation::getMessage)
                .contains("La fecha de finalización no puede ser anterior a la fecha de inicio");
    }

    @Test
    void rejectsNotesLongerThanAllowed() {
        ContractForm form = new ContractForm();
        form.setStartDate(LocalDate.of(2026, 6, 1));
        form.setEndDate(LocalDate.of(2026, 6, 1));
        form.setNotes("a".repeat(501));

        Set<ConstraintViolation<ContractForm>> violations = validator.validate(form);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("notes");
    }

    @Test
    void acceptsValidContract() {
        ContractForm form = new ContractForm();
        form.setStartDate(LocalDate.of(2026, 6, 1));
        form.setEndDate(LocalDate.of(2026, 6, 1));
        form.setNotes("Renovación");

        assertThat(validator.validate(form)).isEmpty();
    }
}
