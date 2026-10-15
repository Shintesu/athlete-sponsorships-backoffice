package com.example.sponsorships.controller;

import com.example.sponsorships.dto.BenefitForm;
import com.example.sponsorships.enums.BenefitType;
import com.example.sponsorships.service.BenefitService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/benefits")
public class BenefitController {

    private final BenefitService benefitService;

    public BenefitController(BenefitService benefitService) {
        this.benefitService = benefitService;
    }

    @ModelAttribute("types")
    public BenefitType[] types() {
        return BenefitType.values();
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("benefits", benefitService.findAll());
        return "benefits/list";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("benefitForm", new BenefitForm());
        return "benefits/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("benefitForm") BenefitForm form, BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "benefits/form";
        }
        benefitService.create(form);
        redirectAttributes.addFlashAttribute("successMessage", "Beneficio creado correctamente.");
        return "redirect:/benefits";
    }
}
