package com.example.sponsorships.dto;

import java.time.LocalDate;

public record ExpiringContractView(String athleteName, String discipline, LocalDate endDate, long daysRemaining) {
}
