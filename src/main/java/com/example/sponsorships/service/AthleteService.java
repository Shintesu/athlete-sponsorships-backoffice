package com.example.sponsorships.service;

import com.example.sponsorships.dto.AthleteForm;
import com.example.sponsorships.entity.Athlete;
import com.example.sponsorships.exception.AthleteNotFoundException;
import com.example.sponsorships.repository.AthleteRepository;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AthleteService {

    private final AthleteRepository athleteRepository;

    public AthleteService(AthleteRepository athleteRepository) {
        this.athleteRepository = athleteRepository;
    }

    @Transactional(readOnly = true)
    public List<Athlete> findAll() {
        return athleteRepository.findAll(Sort.by(Sort.Direction.ASC, "fullName"));
    }

    @Transactional(readOnly = true)
    public Athlete findById(Long id) {
        return athleteRepository.findById(id)
                .orElseThrow(() -> new AthleteNotFoundException(id));
    }

    @Transactional
    public Athlete create(AthleteForm form) {
        Athlete athlete = Athlete.builder()
                .fullName(form.getFullName())
                .discipline(form.getDiscipline())
                .country(form.getCountry())
                .status(form.getStatus())
                .build();
        return athleteRepository.save(athlete);
    }

    @Transactional
    public Athlete update(Long id, AthleteForm form) {
        Athlete athlete = findById(id);
        athlete.setFullName(form.getFullName());
        athlete.setDiscipline(form.getDiscipline());
        athlete.setCountry(form.getCountry());
        athlete.setStatus(form.getStatus());
        return athlete;
    }

    public AthleteForm toForm(Athlete athlete) {
        AthleteForm form = new AthleteForm();
        form.setFullName(athlete.getFullName());
        form.setDiscipline(athlete.getDiscipline());
        form.setCountry(athlete.getCountry());
        form.setStatus(athlete.getStatus());
        return form;
    }
}
