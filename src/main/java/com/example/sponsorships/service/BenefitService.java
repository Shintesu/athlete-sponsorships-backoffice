package com.example.sponsorships.service;

import com.example.sponsorships.dto.BenefitForm;
import com.example.sponsorships.entity.Benefit;
import com.example.sponsorships.exception.BenefitInUseException;
import com.example.sponsorships.exception.BenefitNotFoundException;
import com.example.sponsorships.repository.BenefitRepository;
import com.example.sponsorships.repository.ContractRepository;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BenefitService {

    private final BenefitRepository benefitRepository;
    private final ContractRepository contractRepository;

    public BenefitService(BenefitRepository benefitRepository, ContractRepository contractRepository) {
        this.benefitRepository = benefitRepository;
        this.contractRepository = contractRepository;
    }

    @Transactional(readOnly = true)
    public List<Benefit> findAll() {
        return benefitRepository.findAll(Sort.by(Sort.Direction.ASC, "name"));
    }

    @Transactional(readOnly = true)
    public Benefit findById(Long id) {
        return benefitRepository.findById(id)
                .orElseThrow(() -> new BenefitNotFoundException(id));
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

    @Transactional
    public Benefit update(Long id, BenefitForm form) {
        Benefit benefit = findById(id);
        benefit.setName(form.getName());
        benefit.setType(form.getType());
        benefit.setDescription(form.getDescription());
        return benefit;
    }

    @Transactional
    public void delete(Long id) {
        Benefit benefit = findById(id);
        if (contractRepository.existsByBenefitsId(id)) {
            throw new BenefitInUseException(benefit.getName());
        }
        benefitRepository.delete(benefit);
    }

    public BenefitForm toForm(Benefit benefit) {
        BenefitForm form = new BenefitForm();
        form.setName(benefit.getName());
        form.setType(benefit.getType());
        form.setDescription(benefit.getDescription());
        return form;
    }
}
