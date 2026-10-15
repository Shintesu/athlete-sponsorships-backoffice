package com.example.sponsorships.service;

import com.example.sponsorships.dto.BenefitForm;
import com.example.sponsorships.entity.Benefit;
import com.example.sponsorships.repository.BenefitRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BenefitService {

    private final BenefitRepository benefitRepository;

    public BenefitService(BenefitRepository benefitRepository) {
        this.benefitRepository = benefitRepository;
    }

    @Transactional(readOnly = true)
    public List<Benefit> findAll() {
        return benefitRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Transactional(readOnly = true)
    public List<Benefit> findAllById(Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return benefitRepository.findAllById(ids);
    }

    @Transactional
    public Benefit create(BenefitForm form) {
        Benefit benefit = Benefit.builder()
                .name(form.getName())
                .type(form.getType())
                .description(form.getDescription())
                .build();
        return benefitRepository.save(benefit);
    }
}
