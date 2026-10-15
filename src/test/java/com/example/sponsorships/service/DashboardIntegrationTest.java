package com.example.sponsorships.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.sponsorships.dto.DashboardView;
import com.example.sponsorships.dto.ExpiringContractView;
import com.example.sponsorships.entity.Athlete;
import com.example.sponsorships.entity.Contract;
import com.example.sponsorships.enums.AthleteStatus;
import com.example.sponsorships.enums.ContractStatus;
import com.example.sponsorships.repository.AthleteRepository;
import com.example.sponsorships.repository.ContractRepository;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DashboardIntegrationTest {

    @Autowired
    private AthleteRepository athleteRepository;

    @Autowired
    private ContractRepository contractRepository;

    @Autowired
    private ContractService contractService;

    @Autowired
    private DashboardService dashboardService;

    @Test
    void countsMatchTheRealStatusOfEachPersistedContract() {
        LocalDate today = LocalDate.now();
        Athlete athlete = athleteRepository.save(athlete("Atleta Activo", AthleteStatus.ACTIVE));
        athleteRepository.save(athlete("Atleta Inactivo", AthleteStatus.INACTIVE));
        contractRepository.save(contract(athlete, today.plusDays(5), today.plusDays(20)));
        contractRepository.save(contract(athlete, today.minusDays(10), today.plusDays(40)));
        contractRepository.save(contract(athlete, today.minusDays(10), today.plusDays(10)));
        contractRepository.save(contract(athlete, today.minusDays(40), today.minusDays(1)));
        contractRepository.save(contract(athlete, today.minusDays(10), today));

        List<Contract> persisted = contractRepository.findAll();
        Map<ContractStatus, Long> expected = new EnumMap<>(ContractStatus.class);
        for (ContractStatus status : ContractStatus.values()) {
            expected.put(status, 0L);
        }
        for (Contract contract : persisted) {
            expected.merge(contractService.determineStatus(contract), 1L, Long::sum);
        }

        DashboardView view = dashboardService.load();

        assertThat(view.activeAthletes()).isEqualTo(athleteRepository.countByStatus(AthleteStatus.ACTIVE));
        assertThat(view.contractCounts()).isEqualTo(expected);
        assertThat(view.upcomingExpirations())
                .extracting(ExpiringContractView::endDate)
                .isSorted();
        assertThat(view.upcomingExpirations())
                .allMatch(expiring -> expiring.daysRemaining() >= 0 && expiring.daysRemaining() <= 30);
    }

    private Athlete athlete(String fullName, AthleteStatus status) {
        return Athlete.builder()
                .fullName(fullName)
                .discipline("Ciclismo")
                .country("Colombia")
                .status(status)
                .build();
    }

    private Contract contract(Athlete athlete, LocalDate startDate, LocalDate endDate) {
        return Contract.builder()
                .athlete(athlete)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
