package com.example.sponsorships.config;

import com.example.sponsorships.dto.AthleteForm;
import com.example.sponsorships.dto.BenefitForm;
import com.example.sponsorships.dto.ContractForm;
import com.example.sponsorships.entity.Athlete;
import com.example.sponsorships.entity.Benefit;
import com.example.sponsorships.enums.AthleteStatus;
import com.example.sponsorships.enums.BenefitType;
import com.example.sponsorships.service.AthleteService;
import com.example.sponsorships.service.BenefitService;
import com.example.sponsorships.service.ContractService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
public class DemoDataLoader implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataLoader.class);

    private final AthleteService athleteService;
    private final BenefitService benefitService;
    private final ContractService contractService;
    private final Clock clock;

    public DemoDataLoader(AthleteService athleteService, BenefitService benefitService,
            ContractService contractService, Clock clock) {
        this.athleteService = athleteService;
        this.benefitService = benefitService;
        this.contractService = contractService;
        this.clock = clock;
    }

    @Override
    public void run(String... args) {
        if (!athleteService.findAll().isEmpty()) {
            log.info("Demo data already present, skipping load");
            return;
        }
        LocalDate today = LocalDate.now(clock);

        Benefit uniform = createBenefit("Uniforme oficial", BenefitType.EQUIPMENT,
                "Kit de competición con la marca del patrocinador.");
        Benefit trainingKit = createBenefit("Equipamiento de entrenamiento", BenefitType.EQUIPMENT,
                "Material técnico para la preparación diaria.");
        Benefit monthlyBonus = createBenefit("Bonificación mensual", BenefitType.PAYMENT,
                "Pago mensual por rendimiento deportivo.");
        Benefit scholarship = createBenefit("Beca de formación", BenefitType.PAYMENT,
                "Cobertura de estudios y formación complementaria.");
        Benefit brandEvent = createBenefit("Participación en evento de marca", BenefitType.EVENT,
                "Presencia en eventos promocionales del patrocinador.");
        Benefit summerCamp = createBenefit("Campus de verano", BenefitType.EVENT,
                "Participación en el campus deportivo anual.");

        // The relative dates keep every contract state represented however much time passes.
        Athlete laura = createAthlete("Laura Gómez", "Natación", "España");
        addContract(laura, today.minusDays(400), today.minusDays(100),
                "Contrato histórico de la temporada pasada.", uniform);
        addContract(laura, today.minusDays(60), today.plusDays(120),
                "Renovación vigente con mejoras.", uniform, monthlyBonus);

        Athlete andres = createAthlete("Andrés Ruiz", "Ciclismo", "Colombia");
        addContract(andres, today.minusDays(300), today.plusDays(21),
                "Contrato que vence en tres semanas.", trainingKit);

        Athlete carlos = createAthlete("Carlos Pérez", "Atletismo", "México");
        addContract(carlos, today.minusDays(330), today.plusDays(13),
                "Contrato próximo a vencer.", monthlyBonus, brandEvent);

        Athlete sofia = createAthlete("Sofía Martínez", "Tenis", "Argentina");
        addContract(sofia, today.plusDays(30), today.plusDays(200),
                "Contrato en preparación.", summerCamp);

        Athlete valentina = createAthlete("Valentina Torres", "Gimnasia", "Chile");
        addContract(valentina, today.minusDays(30), today.plusDays(90),
                "Contrato vigente.", scholarship);

        Athlete diego = createAthlete("Diego Fernández", "Baloncesto", "España");
        addContract(diego, today.minusDays(500), today.minusDays(150),
                "Contrato histórico.", trainingKit);
        addContract(diego, today.minusDays(200), today.plusDays(28),
                "Contrato que sigue vigente tras la baja del atleta.", uniform, scholarship);
        markInactive(diego);

        log.info("Demo data loaded");
    }

    private Benefit createBenefit(String name, BenefitType type, String description) {
        BenefitForm form = new BenefitForm();
        form.setName(name);
        form.setType(type);
        form.setDescription(description);
        return benefitService.create(form);
    }

    private Athlete createAthlete(String fullName, String discipline, String country) {
        AthleteForm form = new AthleteForm();
        form.setFullName(fullName);
        form.setDiscipline(discipline);
        form.setCountry(country);
        form.setStatus(AthleteStatus.ACTIVE);
        return athleteService.create(form);
    }

    private void addContract(Athlete athlete, LocalDate startDate, LocalDate endDate, String notes,
            Benefit... benefits) {
        ContractForm form = new ContractForm();
        form.setStartDate(startDate);
        form.setEndDate(endDate);
        form.setNotes(notes);
        form.setBenefitIds(Arrays.stream(benefits).map(Benefit::getId).toList());
        contractService.createContract(athlete.getId(), form);
    }

    private void markInactive(Athlete athlete) {
        AthleteForm form = athleteService.toForm(athlete);
        form.setStatus(AthleteStatus.INACTIVE);
        athleteService.update(athlete.getId(), form);
    }
}
