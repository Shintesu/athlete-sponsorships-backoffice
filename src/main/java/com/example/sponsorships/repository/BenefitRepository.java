package com.example.sponsorships.repository;

import com.example.sponsorships.entity.Benefit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenefitRepository extends JpaRepository<Benefit, Long> {
}
