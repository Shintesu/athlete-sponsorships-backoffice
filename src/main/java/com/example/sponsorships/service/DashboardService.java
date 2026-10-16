package com.example.sponsorships.service;

import com.example.sponsorships.dto.DashboardView;
import com.example.sponsorships.dto.ExpiringContractView;
import com.example.sponsorships.entity.Contract;
import com.example.sponsorships.enums.AthleteStatus;
import com.example.sponsorships.enums.ContractStatus;
import com.example.sponsorships.repository.AthleteRepository;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final List<ContractStatus> STATUS_DISPLAY_ORDER = List.of(
            ContractStatus.IN_FORCE, ContractStatus.EXPIRING, ContractStatus.EXPIRED, ContractStatus.DRAFT);

    private final AthleteRepository athleteRepository;
    private final ContractService contractService;

    public DashboardService(AthleteRepository athleteRepository, ContractService contractService) {
        this.athleteRepository = athleteRepository;
        this.contractService = contractService;
    }

    @Transactional(readOnly = true)
    public DashboardView load() {
        long activeAthletes = athleteRepository.countByStatus(AthleteStatus.ACTIVE);
        // Classify each contract once through ContractService, so counts and the expiring list share the same status.
        List<Classified> classified = contractService.findAll().stream()
                .map(contract -> new Classified(contract, contractService.determineStatus(contract)))
                .toList();
        return new DashboardView(activeAthletes, countByStatus(classified), upcomingExpirations(classified));
    }

    private Map<ContractStatus, Long> countByStatus(List<Classified> classified) {
        Map<ContractStatus, Long> counts = new LinkedHashMap<>();
        for (ContractStatus status : STATUS_DISPLAY_ORDER) {
            counts.put(status, 0L);
        }
        for (Classified entry : classified) {
            counts.merge(entry.status(), 1L, Long::sum);
        }
        return counts;
    }

    private List<ExpiringContractView> upcomingExpirations(List<Classified> classified) {
        return classified.stream()
                .filter(entry -> entry.status() == ContractStatus.EXPIRING)
                .map(Classified::contract)
                .sorted(Comparator.comparing(Contract::getEndDate))
                .map(contract -> new ExpiringContractView(
                        contract.getAthlete().getId(),
                        contract.getId(),
                        contract.getAthlete().getFullName(),
                        contract.getAthlete().getDiscipline(),
                        contract.getEndDate(),
                        contractService.daysUntilEnd(contract)))
                .toList();
    }

    private record Classified(Contract contract, ContractStatus status) {
    }
}
