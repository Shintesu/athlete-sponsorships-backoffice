package com.example.sponsorships.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sponsorships.dto.AthleteForm;
import com.example.sponsorships.dto.ContractForm;
import com.example.sponsorships.entity.Athlete;
import com.example.sponsorships.entity.Contract;
import com.example.sponsorships.enums.AthleteStatus;
import com.example.sponsorships.repository.AthleteRepository;
import com.example.sponsorships.repository.BenefitRepository;
import com.example.sponsorships.repository.ContractRepository;
import java.time.Clock;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AthletePersistenceIntegrationTest {

    @Autowired
    private AthleteService athleteService;

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
    void persistsAthleteWithActiveStatus() {
        Athlete created = athleteService.create(form("Laura Gomez", "Natacion", "Colombia", AthleteStatus.ACTIVE));

        assertThat(created.getId()).isNotNull();
        Athlete reloaded = athleteRepository.findById(created.getId()).orElseThrow();
        assertThat(reloaded.getFullName()).isEqualTo("Laura Gomez");
        assertThat(reloaded.getDiscipline()).isEqualTo("Natacion");
        assertThat(reloaded.getCountry()).isEqualTo("Colombia");
        assertThat(reloaded.getStatus()).isEqualTo(AthleteStatus.ACTIVE);
    }

    @Test
    void keepsContractsWhenAthleteIsMarkedInactive() {
        LocalDate today = LocalDate.now(clock);
        Athlete athlete = athleteService.create(form("Carlos Perez", "Atletismo", "Mexico", AthleteStatus.ACTIVE));
        ContractForm contractForm = new ContractForm();
        contractForm.setStartDate(today.minusDays(10));
        contractForm.setEndDate(today.plusDays(40));
        Contract contract = contractService.createContract(athlete.getId(), contractForm);

        athleteService.update(athlete.getId(), form("Carlos Perez", "Atletismo", "Mexico", AthleteStatus.INACTIVE));

        assertThat(athleteRepository.findById(athlete.getId()).orElseThrow().getStatus())
                .isEqualTo(AthleteStatus.INACTIVE);
        assertThat(contractRepository.findById(contract.getId())).isPresent();
    }

    @Test
    void listsAthletesOrderedByFullName() {
        athleteService.create(form("Zoe Ramirez", "Tenis", "Chile", AthleteStatus.ACTIVE));
        athleteService.create(form("Ana Torres", "Gimnasia", "Peru", AthleteStatus.ACTIVE));

        assertThat(athleteService.findAll())
                .extracting(Athlete::getFullName)
                .containsExactly("Ana Torres", "Zoe Ramirez");
    }

    private AthleteForm form(String fullName, String discipline, String country, AthleteStatus status) {
        AthleteForm form = new AthleteForm();
        form.setFullName(fullName);
        form.setDiscipline(discipline);
        form.setCountry(country);
        form.setStatus(status);
        return form;
    }
}
