package com.example.sponsorships.repository;

import com.example.sponsorships.entity.Contract;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    List<Contract> findByAthleteIdOrderByStartDateDesc(Long athleteId);
}
