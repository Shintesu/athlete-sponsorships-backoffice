package com.example.sponsorships.dto;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AthleteFormValidationTest {

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
    void rejectsBlankRequiredFields() {
        AthleteForm form = new AthleteForm();
        form.setFullName(" ");
        form.setDiscipline("");
        form.setCountry(null);

        Set<ConstraintViolation<AthleteForm>> violations = validator.validate(form);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("fullName", "discipline", "country");
    }

    @Test
    void rejectsFieldsLongerThanAllowed() {
        AthleteForm form = new AthleteForm();
        form.setFullName("a".repeat(151));
        form.setDiscipline("b".repeat(101));
        form.setCountry("c".repeat(101));

        Set<ConstraintViolation<AthleteForm>> violations = validator.validate(form);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("fullName", "discipline", "country");
    }

    @Test
    void acceptsValidForm() {
        AthleteForm form = new AthleteForm();
        form.setFullName("Laura Gomez");
        form.setDiscipline("Natacion");
        form.setCountry("Colombia");

        assertThat(validator.validate(form)).isEmpty();
    }
}
