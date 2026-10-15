package com.example.sponsorships.repository;

import com.example.sponsorships.entity.Athlete;
import com.example.sponsorships.enums.AthleteStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AthleteRepository extends JpaRepository<Athlete, Long> {

    long countByStatus(AthleteStatus status);
}
