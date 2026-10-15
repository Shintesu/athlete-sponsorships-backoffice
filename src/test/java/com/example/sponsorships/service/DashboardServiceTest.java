package com.example.sponsorships.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.example.sponsorships.dto.DashboardView;
import com.example.sponsorships.dto.ExpiringContractView;
import com.example.sponsorships.entity.Athlete;
import com.example.sponsorships.entity.Contract;
import com.example.sponsorships.enums.AthleteStatus;
import com.example.sponsorships.enums.ContractStatus;
import com.example.sponsorships.repository.AthleteRepository;
import com.example.sponsorships.repository.ContractRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 1);
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private AthleteRepository athleteRepository;

    @Mock
    private AthleteService athleteService;

    @Mock
    private BenefitService benefitService;

    private ContractService contractService;
    private DashboardService dashboardService;

    @BeforeEach
    void setUp() {
        contractService = new ContractService(contractRepository, athleteService, benefitService, FIXED_CLOCK);
        dashboardService = new DashboardService(athleteRepository, contractService);
    }

    @Test
    void countsEachContractAccordingToItsRealStatus() {
        List<Contract> contracts = List.of(
                contract(1L, TODAY.plusDays(5), TODAY.plusDays(20)),
                contract(2L, TODAY.minusDays(10), TODAY.plusDays(40)),
                contract(3L, TODAY.minusDays(10), TODAY.plusDays(10)),
                contract(4L, TODAY.minusDays(40), TODAY.minusDays(1)));
        when(contractRepository.findAll()).thenReturn(contracts);
        when(athleteRepository.countByStatus(AthleteStatus.ACTIVE)).thenReturn(3L);

        DashboardView view = dashboardService.load();

        assertThat(view.activeAthletes()).isEqualTo(3L);
        assertThat(view.contractCounts())
                .containsEntry(ContractStatus.DRAFT, 1L)
                .containsEntry(ContractStatus.IN_FORCE, 1L)
                .containsEntry(ContractStatus.EXPIRING, 1L)
                .containsEntry(ContractStatus.EXPIRED, 1L);
        assertThat(view.contractCounts().values().stream().mapToLong(Long::longValue).sum())
                .isEqualTo(contracts.size());
    }

    @Test
    void treatsContractEndingTodayAsExpiring() {
        Contract endsToday = contract(1L, TODAY.minusDays(10), TODAY);
        when(contractRepository.findAll()).thenReturn(List.of(endsToday));
        when(athleteRepository.countByStatus(AthleteStatus.ACTIVE)).thenReturn(0L);

        DashboardView view = dashboardService.load();

        assertThat(view.contractCounts()).containsEntry(ContractStatus.EXPIRING, 1L);
        assertThat(view.upcomingExpirations())
                .singleElement()
                .extracting(ExpiringContractView::daysRemaining)
                .isEqualTo(0L);
    }

    @Test
    void appliesThirtyDayWindowBoundary() {
        Contract expiring = contract(1L, TODAY.minusDays(5), TODAY.plusDays(30));
        Contract inForce = contract(2L, TODAY.minusDays(5), TODAY.plusDays(31));
        when(contractRepository.findAll()).thenReturn(List.of(expiring, inForce));
        when(athleteRepository.countByStatus(AthleteStatus.ACTIVE)).thenReturn(0L);

        DashboardView view = dashboardService.load();

        assertThat(view.contractCounts())
                .containsEntry(ContractStatus.EXPIRING, 1L)
                .containsEntry(ContractStatus.IN_FORCE, 1L);
        assertThat(view.upcomingExpirations())
                .extracting(ExpiringContractView::endDate)
                .containsExactly(TODAY.plusDays(30));
    }

    @Test
    void ordersUpcomingExpirationsFromNearestToFarthest() {
        Contract farther = contract(1L, TODAY.minusDays(10), TODAY.plusDays(20));
        Contract nearer = contract(2L, TODAY.minusDays(10), TODAY.plusDays(5));
        when(contractRepository.findAll()).thenReturn(List.of(farther, nearer));
        when(athleteRepository.countByStatus(AthleteStatus.ACTIVE)).thenReturn(0L);

        List<ExpiringContractView> expirations = dashboardService.load().upcomingExpirations();

        assertThat(expirations)
                .extracting(ExpiringContractView::endDate)
                .containsExactly(TODAY.plusDays(5), TODAY.plusDays(20));
    }

    @Test
    void onlyListsExpiringContractsAmongUpcomingExpirations() {
        Contract draft = contract(1L, TODAY.plusDays(10), TODAY.plusDays(20));
        Contract expired = contract(2L, TODAY.minusDays(40), TODAY.minusDays(1));
        when(contractRepository.findAll()).thenReturn(List.of(draft, expired));
        when(athleteRepository.countByStatus(AthleteStatus.ACTIVE)).thenReturn(0L);

        assertThat(dashboardService.load().upcomingExpirations()).isEmpty();
    }

    private Contract contract(Long id, LocalDate startDate, LocalDate endDate) {
        Athlete athlete = Athlete.builder()
                .id(1L)
                .fullName("Atleta Uno")
                .discipline("Ciclismo")
                .country("Colombia")
                .status(AthleteStatus.ACTIVE)
                .build();
        return Contract.builder()
                .id(id)
                .athlete(athlete)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
}
