package com.example.sponsorships.controller;

import com.example.sponsorships.dto.AthleteForm;
import com.example.sponsorships.entity.Athlete;
import com.example.sponsorships.enums.AthleteStatus;
import com.example.sponsorships.service.AthleteService;
import com.example.sponsorships.service.ContractService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/athletes")
public class AthleteController {

    private final AthleteService athleteService;
    private final ContractService contractService;

    public AthleteController(AthleteService athleteService, ContractService contractService) {
        this.athleteService = athleteService;
        this.contractService = contractService;
    }

    @ModelAttribute("statuses")
    public AthleteStatus[] statuses() {
        return AthleteStatus.values();
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("athletes", athleteService.findAll());
        return "athletes/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("athleteForm", new AthleteForm());
        return "athletes/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("athleteForm") AthleteForm form, BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "athletes/form";
        }
        Athlete athlete = athleteService.create(form);
        redirectAttributes.addFlashAttribute("successMessage", "Atleta creado correctamente.");
        return "redirect:/athletes/" + athlete.getId();
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        model.addAttribute("athlete", athleteService.findById(id));
        model.addAttribute("contractViews", contractService.findViewsByAthlete(id));
        return "athletes/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("athleteForm", athleteService.toForm(athleteService.findById(id)));
        model.addAttribute("athleteId", id);
        return "athletes/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("athleteForm") AthleteForm form,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("athleteId", id);
            return "athletes/form";
        }
        athleteService.update(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Atleta actualizado correctamente.");
        return "redirect:/athletes/" + id;
    }
}
