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
import com.example.sponsorships.exception.InactiveAthleteException;
import com.example.sponsorships.exception.OverlappingContractException;
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
class ContractPersistenceIntegrationTest {

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
    void persistsContractWithItsBenefitsInTheJoinTable() {
        LocalDate today = LocalDate.now(clock);
        Athlete athlete = athleteService.create(athleteForm("Laura Gomez", AthleteStatus.ACTIVE));
        Benefit uniform = benefitService.create(benefitForm("Uniforme oficial", BenefitType.EQUIPMENT));
        Benefit bonus = benefitService.create(benefitForm("Bonificacion mensual", BenefitType.PAYMENT));

        ContractForm form = contractForm(today.minusDays(10), today.plusDays(120));
        form.setBenefitIds(List.of(uniform.getId(), bonus.getId()));
        Contract created = contractService.createContract(athlete.getId(), form);

        Contract reloaded = contractRepository.findWithBenefitsById(created.getId()).orElseThrow();
        assertThat(reloaded.getBenefits())
                .extracting(Benefit::getName)
                .containsExactlyInAnyOrder("Uniforme oficial", "Bonificacion mensual");
        assertThat(contractRepository.existsByBenefitsId(uniform.getId())).isTrue();
    }

    @Test
    void rejectsContractOverlappingAPersistedContractThatHasNotExpired() {
        LocalDate today = LocalDate.now(clock);
        Athlete athlete = athleteService.create(athleteForm("Andres Ruiz", AthleteStatus.ACTIVE));
        contractService.createContract(athlete.getId(), contractForm(today.minusDays(10), today.plusDays(40)));

        assertThatThrownBy(() -> contractService.createContract(athlete.getId(),
                contractForm(today, today.plusDays(5))))
                .isInstanceOf(OverlappingContractException.class);
        assertThat(contractRepository.findAll()).hasSize(1);
    }

    @Test
    void allowsNewContractSpanningAPersistedExpiredContract() {
        LocalDate today = LocalDate.now(clock);
        Athlete athlete = athleteService.create(athleteForm("Diego Fernandez", AthleteStatus.ACTIVE));
        contractService.createContract(athlete.getId(), contractForm(today.minusDays(40), today.minusDays(1)));

        Contract created = contractService.createContract(athlete.getId(),
                contractForm(today.minusDays(5), today.plusDays(10)));

        assertThat(created.getId()).isNotNull();
        assertThat(contractRepository.findAll()).hasSize(2);
    }

    @Test
    void excludesTheEditedContractFromThePersistedOverlapCheck() {
        LocalDate today = LocalDate.now(clock);
        Athlete athlete = athleteService.create(athleteForm("Valentina Torres", AthleteStatus.ACTIVE));
        Contract contract = contractService.createContract(athlete.getId(),
                contractForm(today, today.plusDays(30)));

        contractService.updateContract(contract.getId(), contractForm(today, today.plusDays(60)));

        assertThat(contractRepository.findById(contract.getId()).orElseThrow().getEndDate())
                .isEqualTo(today.plusDays(60));
    }

    @Test
    void rejectsContractForAnInactivePersistedAthlete() {
        LocalDate today = LocalDate.now(clock);
        Athlete athlete = athleteService.create(athleteForm("Sofia Martinez", AthleteStatus.ACTIVE));
        AthleteForm inactive = athleteForm("Sofia Martinez", AthleteStatus.INACTIVE);
        athleteService.update(athlete.getId(), inactive);

        assertThatThrownBy(() -> contractService.createContract(athlete.getId(),
                contractForm(today, today.plusDays(30))))
                .isInstanceOf(InactiveAthleteException.class);
    }

    private AthleteForm athleteForm(String fullName, AthleteStatus status) {
        AthleteForm form = new AthleteForm();
        form.setFullName(fullName);
        form.setDiscipline("Ciclismo");
        form.setCountry("Colombia");
        form.setStatus(status);
        return form;
    }

    private BenefitForm benefitForm(String name, BenefitType type) {
        BenefitForm form = new BenefitForm();
        form.setName(name);
        form.setType(type);
        form.setDescription(null);
        return form;
    }

    private ContractForm contractForm(LocalDate startDate, LocalDate endDate) {
        ContractForm form = new ContractForm();
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        return form;
    }
}
