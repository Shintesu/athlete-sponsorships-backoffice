package com.example.sponsorships.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.sponsorships.dto.BenefitForm;
import com.example.sponsorships.entity.Benefit;
import com.example.sponsorships.enums.BenefitType;
import com.example.sponsorships.repository.BenefitRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BenefitServiceTest {

    @Mock
    private BenefitRepository benefitRepository;

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
}
