package com.example.sponsorships.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.sponsorships.dto.BenefitForm;
import com.example.sponsorships.entity.Benefit;
import com.example.sponsorships.enums.BenefitType;
import com.example.sponsorships.exception.BenefitInUseException;
import com.example.sponsorships.exception.BenefitNotFoundException;
import com.example.sponsorships.repository.BenefitRepository;
import com.example.sponsorships.repository.ContractRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BenefitServiceTest {

    @Mock
    private BenefitRepository benefitRepository;

    @Mock
    private ContractRepository contractRepository;

    @InjectMocks
    private BenefitService benefitService;

    @Test
    void createsBenefitFromForm() {
        BenefitForm form = new BenefitForm();
        form.setName("Uniforme oficial");
        form.setType(BenefitType.EQUIPMENT);
        form.setDescription("Kit completo de competición");
        when(benefitRepository.save(any(Benefit.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Benefit created = benefitService.create(form);

        assertThat(created.getName()).isEqualTo("Uniforme oficial");
        assertThat(created.getType()).isEqualTo(BenefitType.EQUIPMENT);
        assertThat(created.getDescription()).isEqualTo("Kit completo de competición");
        verify(benefitRepository).save(created);
    }

    @Test
    void returnsEmptyListWhenNoIdsAreGiven() {
        assertThat(benefitService.findAllById(List.of())).isEmpty();

        verifyNoInteractions(benefitRepository);
    }

    @Test
    void updatesBenefitFromForm() {
        Benefit benefit = Benefit.builder()
                .id(1L)
                .name("Uniforme oficial")
                .type(BenefitType.EQUIPMENT)
                .description("Kit completo")
                .build();
        when(benefitRepository.findById(1L)).thenReturn(Optional.of(benefit));
        BenefitForm form = new BenefitForm();
        form.setName("Uniforme de gala");
        form.setType(BenefitType.EVENT);
        form.setDescription("Kit de presentación");

        Benefit updated = benefitService.update(1L, form);

        assertThat(updated.getName()).isEqualTo("Uniforme de gala");
        assertThat(updated.getType()).isEqualTo(BenefitType.EVENT);
        assertThat(updated.getDescription()).isEqualTo("Kit de presentación");
    }

    @Test
    void deletesBenefitWhenNoContractUsesIt() {
        Benefit benefit = Benefit.builder().id(1L).name("Uniforme oficial").type(BenefitType.EQUIPMENT).build();
        when(benefitRepository.findById(1L)).thenReturn(Optional.of(benefit));
        when(contractRepository.existsByBenefitsId(1L)).thenReturn(false);

        benefitService.delete(1L);

        verify(benefitRepository).delete(benefit);
    }

    @Test
    void rejectsDeletingBenefitUsedByContract() {
        Benefit benefit = Benefit.builder().id(1L).name("Uniforme oficial").type(BenefitType.EQUIPMENT).build();
        when(benefitRepository.findById(1L)).thenReturn(Optional.of(benefit));
        when(contractRepository.existsByBenefitsId(1L)).thenReturn(true);

        assertThatThrownBy(() -> benefitService.delete(1L))
                .isInstanceOf(BenefitInUseException.class);

        verify(benefitRepository, never()).delete(any(Benefit.class));
    }

    @Test
    void throwsWhenBenefitDoesNotExist() {
        when(benefitRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> benefitService.findById(99L))
                .isInstanceOf(BenefitNotFoundException.class);

        verifyNoInteractions(contractRepository);
    }
}
