package com.example.sponsorships.repository;

import com.example.sponsorships.entity.Contract;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    @EntityGraph(attributePaths = "benefits")
    List<Contract> findByAthleteIdOrderByStartDateDesc(Long athleteId);

    @EntityGraph(attributePaths = "benefits")
    Optional<Contract> findWithBenefitsById(Long id);
}
