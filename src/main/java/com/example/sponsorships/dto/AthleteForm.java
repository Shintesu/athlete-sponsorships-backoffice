package com.example.sponsorships.dto;

import com.example.sponsorships.enums.AthleteStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AthleteForm {

    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 150, message = "El nombre completo no puede superar los 150 caracteres")
    private String fullName;

    @NotBlank(message = "La disciplina es obligatoria")
    @Size(max = 100, message = "La disciplina no puede superar los 100 caracteres")
    private String discipline;

    @NotBlank(message = "El país es obligatorio")
    @Size(max = 100, message = "El país no puede superar los 100 caracteres")
    private String country;

    @NotNull(message = "El estado es obligatorio")
    private AthleteStatus status = AthleteStatus.ACTIVE;
}
