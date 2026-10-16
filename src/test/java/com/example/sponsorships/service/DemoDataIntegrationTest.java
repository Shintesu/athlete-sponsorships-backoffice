package com.example.sponsorships.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sponsorships.dto.DashboardView;
import com.example.sponsorships.entity.Benefit;
import com.example.sponsorships.entity.Contract;
import com.example.sponsorships.enums.AthleteStatus;
import com.example.sponsorships.enums.BenefitType;
import com.example.sponsorships.repository.AthleteRepository;
import com.example.sponsorships.repository.BenefitRepository;
import com.example.sponsorships.repository.ContractRepository;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"test", "demo"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DemoDataIntegrationTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private BenefitRepository benefitRepository;

    @AfterAll
    void cleanUp() {
        contractRepository.deleteAll();
        benefitRepository.deleteAll();
        athleteRepository.deleteAll();
    }

    @Test
    void loadsEveryDashboardIndicator() {
        DashboardView view = dashboardService.load();

        assertThat(view.activeAthletes()).isPositive();
        assertThat(athleteRepository.countByStatus(AthleteStatus.INACTIVE)).isPositive();
        assertThat(view.contractCounts()).isNotEmpty();
        assertThat(view.contractCounts()).allSatisfy((status, count) -> assertThat(count).isPositive());
        assertThat(view.upcomingExpirations()).isNotEmpty();
        assertThat(view.upcomingExpirations())
                .allMatch(expiring -> expiring.daysRemaining() >= 0 && expiring.daysRemaining() <= 30);
    }

    @Test
    void loadsBenefitsOfEveryTypeAndReuse() {
        List<Benefit> benefits = benefitRepository.findAll();

        assertThat(benefits).extracting(Benefit::getType).contains(BenefitType.values());
        assertThat(contractRepository.findAll().stream()
                .map(Contract::getId)
                .map(id -> contractRepository.findWithBenefitsById(id).orElseThrow())
                .anyMatch(contract -> contract.getBenefits().size() >= 2))
                .isTrue();
    }
}
