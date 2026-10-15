package com.example.sponsorships.dto;

import com.example.sponsorships.enums.ContractStatus;
import java.util.List;
import java.util.Map;

public record DashboardView(long activeAthletes, Map<ContractStatus, Long> contractCounts,
        List<ExpiringContractView> upcomingExpirations) {
}
