package com.example.sponsorships.controller;

import com.example.sponsorships.dto.ContractForm;
import com.example.sponsorships.entity.Contract;
import com.example.sponsorships.exception.InactiveAthleteException;
import com.example.sponsorships.exception.InvalidDateRangeException;
import com.example.sponsorships.exception.OverlappingContractException;
import com.example.sponsorships.service.AthleteService;
import com.example.sponsorships.service.BenefitService;
import com.example.sponsorships.service.ContractService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContractController {

    private final ContractService contractService;
    private final AthleteService athleteService;
    private final BenefitService benefitService;

    public ContractController(ContractService contractService, AthleteService athleteService,
            BenefitService benefitService) {
        this.contractService = contractService;
        this.athleteService = athleteService;
        this.benefitService = benefitService;
    }

    @GetMapping("/athletes/{athleteId}/contracts/new")
    public String createForm(@PathVariable Long athleteId, Model model) {
        model.addAttribute("athlete", athleteService.findById(athleteId));
        model.addAttribute("contractForm", new ContractForm());
        model.addAttribute("benefits", benefitService.findAll());
        return "contracts/form";
    }

    @PostMapping("/athletes/{athleteId}/contracts")
    public String create(@PathVariable Long athleteId, @Valid @ModelAttribute("contractForm") ContractForm form,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                Contract contract = contractService.createContract(athleteId, form);
                redirectAttributes.addFlashAttribute("successMessage", "Contrato creado correctamente.");
                return "redirect:/contracts/" + contract.getId();
            } catch (InactiveAthleteException | InvalidDateRangeException | OverlappingContractException exception) {
                bindingResult.reject("contract", exception.getMessage());
            }
        }
        model.addAttribute("athlete", athleteService.findById(athleteId));
        model.addAttribute("benefits", benefitService.findAll());
        return "contracts/form";
    }

    @GetMapping("/contracts/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Contract contract = contractService.findById(id);
        model.addAttribute("contract", contract);
        model.addAttribute("status", contractService.determineStatus(contract));
        return "contracts/detail";
    }

    @GetMapping("/contracts/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Contract contract = contractService.findById(id);
        model.addAttribute("contractForm", contractService.toForm(contract));
        model.addAttribute("contractId", id);
        model.addAttribute("athlete", contract.getAthlete());
        model.addAttribute("benefits", benefitService.findAll());
        return "contracts/form";
    }

    @PostMapping("/contracts/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("contractForm") ContractForm form,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (!bindingResult.hasErrors()) {
            try {
                contractService.updateContract(id, form);
                redirectAttributes.addFlashAttribute("successMessage", "Contrato actualizado correctamente.");
                return "redirect:/contracts/" + id;
            } catch (InvalidDateRangeException | OverlappingContractException exception) {
                bindingResult.reject("contract", exception.getMessage());
            }
        }
        Contract contract = contractService.findById(id);
        model.addAttribute("contractId", id);
        model.addAttribute("athlete", contract.getAthlete());
        model.addAttribute("benefits", benefitService.findAll());
        return "contracts/form";
    }
}
