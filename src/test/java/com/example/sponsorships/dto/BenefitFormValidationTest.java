package com.example.sponsorships.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sponsorships.enums.BenefitType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BenefitFormValidationTest {

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
    void rejectsMissingNameAndType() {
        BenefitForm form = new BenefitForm();

        Set<ConstraintViolation<BenefitForm>> violations = validator.validate(form);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("name", "type");
    }

    @Test
    void rejectsDescriptionLongerThanAllowed() {
        BenefitForm form = new BenefitForm();
        form.setName("Uniforme");
        form.setType(BenefitType.EQUIPMENT);
        form.setDescription("a".repeat(501));

        Set<ConstraintViolation<BenefitForm>> violations = validator.validate(form);

        assertThat(violations)
                .extracting(violation -> violation.getPropertyPath().toString())
                .contains("description");
    }

    @Test
    void acceptsValidBenefit() {
        BenefitForm form = new BenefitForm();
        form.setName("Uniforme");
        form.setType(BenefitType.EQUIPMENT);
        form.setDescription("Kit completo");

        assertThat(validator.validate(form)).isEmpty();
    }
}
