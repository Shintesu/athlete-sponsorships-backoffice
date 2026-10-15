package com.example.sponsorships.service;

import com.example.sponsorships.dto.ContractForm;
import com.example.sponsorships.dto.ContractView;
import com.example.sponsorships.entity.Athlete;
import com.example.sponsorships.entity.Benefit;
import com.example.sponsorships.entity.Contract;
import com.example.sponsorships.enums.AthleteStatus;
import com.example.sponsorships.enums.ContractStatus;
import com.example.sponsorships.exception.ContractNotFoundException;
import com.example.sponsorships.exception.InactiveAthleteException;
import com.example.sponsorships.exception.InvalidDateRangeException;
import com.example.sponsorships.exception.OverlappingContractException;
import com.example.sponsorships.repository.ContractRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContractService {

    static final long EXPIRING_WINDOW_DAYS = 30;

    private final ContractRepository contractRepository;
    private final AthleteService athleteService;
    private final BenefitService benefitService;
    private final Clock clock;

    public ContractService(ContractRepository contractRepository, AthleteService athleteService,
            BenefitService benefitService, Clock clock) {
        this.contractRepository = contractRepository;
        this.athleteService = athleteService;
        this.benefitService = benefitService;
        this.clock = clock;
    }

    @Transactional
    public Contract createContract(Long athleteId, ContractForm form) {
        Athlete athlete = athleteService.findById(athleteId);
        if (athlete.getStatus() == AthleteStatus.INACTIVE) {
            throw new InactiveAthleteException(athlete.getFullName());
        }
        validateDateRange(form);
        ensureNoOverlap(athlete.getId(), form.getStartDate(), form.getEndDate(), null);
        Contract contract = Contract.builder()
                .athlete(athlete)
                .startDate(form.getStartDate())
                .endDate(form.getEndDate())
                .notes(form.getNotes())
                .benefits(resolveBenefits(form.getBenefitIds()))
                .build();
        return contractRepository.save(contract);
    }

    @Transactional
    public Contract updateContract(Long id, ContractForm form) {
        Contract contract = findById(id);
        validateDateRange(form);
        ensureNoOverlap(contract.getAthlete().getId(), form.getStartDate(), form.getEndDate(), id);
        contract.setStartDate(form.getStartDate());
        contract.setEndDate(form.getEndDate());
        contract.setNotes(form.getNotes());
        contract.setBenefits(resolveBenefits(form.getBenefitIds()));
        return contract;
    }

    @Transactional(readOnly = true)
    public Contract findById(Long id) {
        return contractRepository.findWithBenefitsById(id)
                .orElseThrow(() -> new ContractNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Contract> findAll() {
        return contractRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ContractView> findViewsByAthlete(Long athleteId) {
        return findByAthlete(athleteId).stream()
                .map(contract -> new ContractView(contract, determineStatus(contract)))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Contract> findByAthlete(Long athleteId) {
        return contractRepository.findByAthleteIdOrderByStartDateDesc(athleteId);
    }

    public ContractStatus determineStatus(Contract contract) {
        LocalDate today = LocalDate.now(clock);
        if (today.isBefore(contract.getStartDate())) {
            return ContractStatus.DRAFT;
        }
        if (today.isAfter(contract.getEndDate())) {
            return ContractStatus.EXPIRED;
        }
        // endDate is inclusive: a contract ending today is still inside the 30-day window (SPECS §13).
        if (daysUntilEnd(contract) <= EXPIRING_WINDOW_DAYS) {
            return ContractStatus.EXPIRING;
        }
        return ContractStatus.IN_FORCE;
    }

    public long daysUntilEnd(Contract contract) {
        return ChronoUnit.DAYS.between(LocalDate.now(clock), contract.getEndDate());
    }

    public ContractForm toForm(Contract contract) {
        ContractForm form = new ContractForm();
        form.setStartDate(contract.getStartDate());
        form.setEndDate(contract.getEndDate());
        form.setNotes(contract.getNotes());
        form.setBenefitIds(contract.getBenefits().stream().map(Benefit::getId).toList());
        return form;
    }

    private List<Benefit> resolveBenefits(List<Long> benefitIds) {
        if (benefitIds == null || benefitIds.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(benefitService.findAllById(benefitIds));
    }

    private void validateDateRange(ContractForm form) {
        if (form.getEndDate().isBefore(form.getStartDate())) {
            throw new InvalidDateRangeException();
        }
    }

    private void ensureNoOverlap(Long athleteId, LocalDate startDate, LocalDate endDate, Long excludedContractId) {
        boolean overlaps = findByAthlete(athleteId).stream()
                .filter(contract -> !contract.getId().equals(excludedContractId))
                .filter(contract -> determineStatus(contract) != ContractStatus.EXPIRED)
                .anyMatch(contract -> !startDate.isAfter(contract.getEndDate())
                        && !contract.getStartDate().isAfter(endDate));
        if (overlaps) {
            throw new OverlappingContractException();
        }
    }
}
