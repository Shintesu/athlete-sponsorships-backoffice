package com.example.sponsorships.dto;

import java.time.LocalDate;

public record ExpiringContractView(Long athleteId, Long contractId, String athleteName, String discipline,
        LocalDate endDate, long daysRemaining) {
}
