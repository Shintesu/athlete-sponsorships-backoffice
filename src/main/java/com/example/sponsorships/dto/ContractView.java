package com.example.sponsorships.dto;

import com.example.sponsorships.entity.Contract;
import com.example.sponsorships.enums.ContractStatus;

public record ContractView(Contract contract, ContractStatus status) {
}
