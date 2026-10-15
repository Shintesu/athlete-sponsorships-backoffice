package com.example.sponsorships.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.sponsorships.dto.AthleteForm;
import com.example.sponsorships.entity.Athlete;
import com.example.sponsorships.enums.AthleteStatus;
import com.example.sponsorships.exception.AthleteNotFoundException;
import com.example.sponsorships.repository.AthleteRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AthleteServiceTest {

    @Mock
    private AthleteRepository athleteRepository;

    @InjectMocks
    private AthleteService athleteService;

    @Test
    void createsAthleteFromForm() {
        AthleteForm form = new AthleteForm();
        form.setFullName("Laura Gomez");
        form.setDiscipline("Natacion");
        form.setCountry("Colombia");
        form.setStatus(AthleteStatus.ACTIVE);
        when(athleteRepository.save(any(Athlete.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Athlete created = athleteService.create(form);

        assertThat(created.getFullName()).isEqualTo("Laura Gomez");
        assertThat(created.getDiscipline()).isEqualTo("Natacion");
        assertThat(created.getCountry()).isEqualTo("Colombia");
        assertThat(created.getStatus()).isEqualTo(AthleteStatus.ACTIVE);
        verify(athleteRepository).save(created);
    }

    @Test
    void throwsWhenAthleteDoesNotExist() {
        when(athleteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> athleteService.findById(99L))
                .isInstanceOf(AthleteNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void allowsMarkingAthleteAsInactive() {
        Athlete existing = Athlete.builder()
                .id(1L)
                .fullName("Carlos Perez")
                .discipline("Atletismo")
                .country("Mexico")
                .status(AthleteStatus.ACTIVE)
                .build();
        when(athleteRepository.findById(1L)).thenReturn(Optional.of(existing));
        AthleteForm form = new AthleteForm();
        form.setFullName("Carlos Perez");
        form.setDiscipline("Atletismo");
        form.setCountry("Mexico");
        form.setStatus(AthleteStatus.INACTIVE);

        Athlete updated = athleteService.update(1L, form);

        assertThat(updated.getStatus()).isEqualTo(AthleteStatus.INACTIVE);
    }
}
