package com.example.sponsorships.dto;

import com.example.sponsorships.enums.BenefitType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BenefitForm {

    @NotBlank(message = "El nombre del beneficio es obligatorio")
    @Size(max = 100, message = "El nombre del beneficio no puede superar los 100 caracteres")
    private String name;

    @NotNull(message = "El tipo de beneficio es obligatorio")
    private BenefitType type;

    @Size(max = 500, message = "La descripción no puede superar los 500 caracteres")
    private String description;
}
