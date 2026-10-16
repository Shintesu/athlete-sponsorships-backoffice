package com.example.sponsorships.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.sponsorships.dto.AthleteForm;
import com.example.sponsorships.dto.BenefitForm;
import com.example.sponsorships.dto.ContractForm;
import com.example.sponsorships.entity.Athlete;
import com.example.sponsorships.entity.Benefit;
import com.example.sponsorships.entity.Contract;
import com.example.sponsorships.enums.AthleteStatus;
import com.example.sponsorships.enums.BenefitType;
import com.example.sponsorships.exception.BenefitInUseException;
import com.example.sponsorships.repository.AthleteRepository;
import com.example.sponsorships.repository.BenefitRepository;
import com.example.sponsorships.repository.ContractRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class BenefitPersistenceIntegrationTest {

    @Autowired
    private AthleteService athleteService;

    @Autowired
    private BenefitService benefitService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private BenefitRepository benefitRepository;

    @Autowired
    private Clock clock;

    @AfterEach
    void cleanUp() {
        contractRepository.deleteAll();
        benefitRepository.deleteAll();
        athleteRepository.deleteAll();
    }

    @Test
    void rejectsDeletingBenefitUsedByAPersistedContract() {
        LocalDate today = LocalDate.now(clock);
        Athlete athlete = athleteService.create(athleteForm("Laura Gomez"));
        Benefit benefit = benefitService.create(benefitForm("Uniforme oficial", BenefitType.EQUIPMENT));
        ContractForm form = contractForm(today, today.plusDays(30));
        form.setBenefitIds(List.of(benefit.getId()));
        contractService.createContract(athlete.getId(), form);

        assertThatThrownBy(() -> benefitService.delete(benefit.getId()))
                .isInstanceOf(BenefitInUseException.class);
        assertThat(benefitRepository.findById(benefit.getId())).isPresent();
    }

    @Test
    void deletesBenefitWhenNoContractUsesIt() {
        Benefit benefit = benefitService.create(benefitForm("Campus de verano", BenefitType.EVENT));

        benefitService.delete(benefit.getId());

        assertThat(benefitRepository.findById(benefit.getId())).isEmpty();
    }

    @Test
    void persistsUpdatedBenefitFields() {
        Benefit benefit = benefitService.create(benefitForm("Uniforme oficial", BenefitType.EQUIPMENT));
        BenefitForm update = benefitForm("Uniforme de gala", BenefitType.EVENT);
        update.setDescription("Kit de presentacion");

        benefitService.update(benefit.getId(), update);

        Benefit reloaded = benefitRepository.findById(benefit.getId()).orElseThrow();
        assertThat(reloaded.getName()).isEqualTo("Uniforme de gala");
        assertThat(reloaded.getType()).isEqualTo(BenefitType.EVENT);
        assertThat(reloaded.getDescription()).isEqualTo("Kit de presentacion");
    }

    @Test
    void reusesOneBenefitAcrossMultipleContracts() {
        LocalDate today = LocalDate.now(clock);
        Athlete athlete = athleteService.create(athleteForm("Andres Ruiz"));
        Benefit benefit = benefitService.create(benefitForm("Bonificacion mensual", BenefitType.PAYMENT));
        ContractForm expired = contractForm(today.minusDays(60), today.minusDays(10));
        expired.setBenefitIds(List.of(benefit.getId()));
        Contract first = contractService.createContract(athlete.getId(), expired);
        ContractForm current = contractForm(today, today.plusDays(30));
        current.setBenefitIds(List.of(benefit.getId()));
        Contract second = contractService.createContract(athlete.getId(), current);

        assertThat(contractRepository.findWithBenefitsById(first.getId()).orElseThrow().getBenefits())
                .extracting(Benefit::getId)
                .containsExactly(benefit.getId());
        assertThat(contractRepository.findWithBenefitsById(second.getId()).orElseThrow().getBenefits())
                .extracting(Benefit::getId)
                .containsExactly(benefit.getId());
    }

    private AthleteForm athleteForm(String fullName) {
        AthleteForm form = new AthleteForm();
        form.setFullName(fullName);
        form.setDiscipline("Ciclismo");
        form.setCountry("Colombia");
        form.setStatus(AthleteStatus.ACTIVE);
        return form;
    }

    private BenefitForm benefitForm(String name, BenefitType type) {
        BenefitForm form = new BenefitForm();
        form.setName(name);
        form.setType(type);
        return form;
    }

    private ContractForm contractForm(LocalDate startDate, LocalDate endDate) {
        ContractForm form = new ContractForm();
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        return form;
    }
}
