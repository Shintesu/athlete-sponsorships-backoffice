package com.example.sponsorships.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.sponsorships.dto.ContractForm;
import com.example.sponsorships.entity.Athlete;
import com.example.sponsorships.entity.Benefit;
import com.example.sponsorships.entity.Contract;
import com.example.sponsorships.enums.AthleteStatus;
import com.example.sponsorships.enums.BenefitType;
import com.example.sponsorships.enums.ContractStatus;
import com.example.sponsorships.exception.InactiveAthleteException;
import com.example.sponsorships.exception.InvalidDateRangeException;
import com.example.sponsorships.exception.OverlappingContractException;
import com.example.sponsorships.repository.ContractRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ContractServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 1);
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-06-01T00:00:00Z"), ZoneOffset.UTC);

    @Mock
    private ContractRepository contractRepository;

    @Mock
    private AthleteService athleteService;

    @Mock
    private BenefitService benefitService;

    private ContractService contractService;

    @BeforeEach
    void setUp() {
        contractService = new ContractService(contractRepository, athleteService, benefitService, FIXED_CLOCK);
    }

    @Test
    void createsContractForActiveAthlete() {
        Athlete athlete = athlete(1L, AthleteStatus.ACTIVE);
        when(athleteService.findById(1L)).thenReturn(athlete);
        when(contractRepository.findByAthleteIdOrderByStartDateDesc(1L)).thenReturn(List.of());
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contract created = contractService.createContract(1L, form(TODAY, TODAY.plusMonths(6), "nota"));

        assertThat(created.getAthlete()).isEqualTo(athlete);
        assertThat(created.getStartDate()).isEqualTo(TODAY);
        assertThat(created.getEndDate()).isEqualTo(TODAY.plusMonths(6));
        assertThat(created.getNotes()).isEqualTo("nota");
        verify(contractRepository).save(created);
    }

    @Test
    void rejectsContractForInactiveAthlete() {
        when(athleteService.findById(1L)).thenReturn(athlete(1L, AthleteStatus.INACTIVE));

        assertThatThrownBy(() -> contractService.createContract(1L, form(TODAY, TODAY.plusMonths(1), null)))
                .isInstanceOf(InactiveAthleteException.class);
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        when(athleteService.findById(1L)).thenReturn(athlete(1L, AthleteStatus.ACTIVE));

        assertThatThrownBy(() -> contractService.createContract(1L, form(TODAY, TODAY.minusDays(1), null)))
                .isInstanceOf(InvalidDateRangeException.class);
    }

    @Test
    void rejectsOverlapWithNonExpiredContract() {
        Athlete athlete = athlete(1L, AthleteStatus.ACTIVE);
        Contract existing = contract(10L, athlete, TODAY.minusMonths(1), TODAY.plusMonths(1));
        when(athleteService.findById(1L)).thenReturn(athlete);
        when(contractRepository.findByAthleteIdOrderByStartDateDesc(1L)).thenReturn(List.of(existing));

        assertThatThrownBy(() -> contractService.createContract(1L, form(TODAY, TODAY.plusDays(5), null)))
                .isInstanceOf(OverlappingContractException.class);
    }

    @Test
    void rejectsOverlapWithFutureDraftContract() {
        Athlete athlete = athlete(1L, AthleteStatus.ACTIVE);
        Contract future = contract(11L, athlete, TODAY.plusMonths(2), TODAY.plusMonths(4));
        when(athleteService.findById(1L)).thenReturn(athlete);
        when(contractRepository.findByAthleteIdOrderByStartDateDesc(1L)).thenReturn(List.of(future));

        ContractForm form = form(TODAY.plusMonths(1), TODAY.plusMonths(3), null);

        assertThatThrownBy(() -> contractService.createContract(1L, form))
                .isInstanceOf(OverlappingContractException.class);
    }

    @Test
    void allowsNewContractWhenPreviousContractsExpired() {
        Athlete athlete = athlete(1L, AthleteStatus.ACTIVE);
        Contract expired = contract(12L, athlete, TODAY.minusMonths(2), TODAY.minusDays(1));
        when(athleteService.findById(1L)).thenReturn(athlete);
        when(contractRepository.findByAthleteIdOrderByStartDateDesc(1L)).thenReturn(List.of(expired));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Contract created = contractService.createContract(1L, form(TODAY.minusMonths(1), TODAY.plusMonths(1), null));

        assertThat(created).isNotNull();
    }

    @Test
    void excludesTheContractBeingEditedFromOverlapCheck() {
        Athlete athlete = athlete(1L, AthleteStatus.ACTIVE);
        Contract existing = contract(20L, athlete, TODAY, TODAY.plusMonths(1));
        when(contractRepository.findWithBenefitsById(20L)).thenReturn(Optional.of(existing));
        when(contractRepository.findByAthleteIdOrderByStartDateDesc(1L)).thenReturn(List.of(existing));

        Contract updated = contractService.updateContract(20L, form(TODAY.plusDays(1), TODAY.plusMonths(2), "x"));

        assertThat(updated.getEndDate()).isEqualTo(TODAY.plusMonths(2));
    }

    @Test
    void associatesMultipleBenefitsToContract() {
        Athlete athlete = athlete(1L, AthleteStatus.ACTIVE);
        Benefit uniform = benefit(1L, "Uniforme oficial");
        Benefit bonus = benefit(2L, "Bonificación mensual");
        when(athleteService.findById(1L)).thenReturn(athlete);
        when(contractRepository.findByAthleteIdOrderByStartDateDesc(1L)).thenReturn(List.of());
        when(benefitService.findAllById(List.of(1L, 2L))).thenReturn(List.of(uniform, bonus));
        when(contractRepository.save(any(Contract.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ContractForm form = form(TODAY, TODAY.plusMonths(6), null);
        form.setBenefitIds(List.of(1L, 2L));

        Contract created = contractService.createContract(1L, form);

        assertThat(created.getBenefits()).containsExactly(uniform, bonus);
    }

    @Test
    void classifiesContractAsDraft() {
        Contract contract = contract(1L, athlete(1L, AthleteStatus.ACTIVE), TODAY.plusDays(5), TODAY.plusDays(20));

        assertThat(contractService.determineStatus(contract)).isEqualTo(ContractStatus.DRAFT);
    }

    @Test
    void classifiesContractAsInForce() {
        Contract contract = contract(1L, athlete(1L, AthleteStatus.ACTIVE), TODAY.minusDays(10), TODAY.plusDays(31));

        assertThat(contractService.determineStatus(contract)).isEqualTo(ContractStatus.IN_FORCE);
    }

    @Test
    void classifiesContractAsExpiringWhenItEndsToday() {
        Contract contract = contract(1L, athlete(1L, AthleteStatus.ACTIVE), TODAY.minusDays(10), TODAY);

        assertThat(contractService.determineStatus(contract)).isEqualTo(ContractStatus.EXPIRING);
    }

    @Test
    void classifiesContractAsExpiringWithinWindow() {
        Contract contract = contract(1L, athlete(1L, AthleteStatus.ACTIVE), TODAY.minusDays(10), TODAY.plusDays(30));

        assertThat(contractService.determineStatus(contract)).isEqualTo(ContractStatus.EXPIRING);
    }

    @Test
    void classifiesContractAsExpired() {
        Contract contract = contract(1L, athlete(1L, AthleteStatus.ACTIVE), TODAY.minusMonths(2), TODAY.minusDays(1));

        assertThat(contractService.determineStatus(contract)).isEqualTo(ContractStatus.EXPIRED);
    }

    private Athlete athlete(Long id, AthleteStatus status) {
        return Athlete.builder()
                .id(id)
                .fullName("Atleta " + id)
                .discipline("Disciplina")
                .country("Pais")
                .status(status)
                .build();
    }

    private Contract contract(Long id, Athlete athlete, LocalDate startDate, LocalDate endDate) {
        return Contract.builder()
                .id(id)
                .athlete(athlete)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }

    private Benefit benefit(Long id, String name) {
        return Benefit.builder()
                .id(id)
                .name(name)
                .type(BenefitType.EQUIPMENT)
                .build();
    }

    private ContractForm form(LocalDate startDate, LocalDate endDate, String notes) {
        ContractForm form = new ContractForm();
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        form.setNotes(notes);
        return form;
    }
}
