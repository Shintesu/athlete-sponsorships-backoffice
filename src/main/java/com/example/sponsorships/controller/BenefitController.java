package com.example.sponsorships.controller;

import com.example.sponsorships.dto.BenefitForm;
import com.example.sponsorships.enums.BenefitType;
import com.example.sponsorships.exception.BenefitInUseException;
import com.example.sponsorships.service.BenefitService;
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

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("benefitForm", benefitService.toForm(benefitService.findById(id)));
        model.addAttribute("benefitId", id);
        return "benefits/form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute("benefitForm") BenefitForm form,
            BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("benefitId", id);
            return "benefits/form";
        }
        benefitService.update(id, form);
        redirectAttributes.addFlashAttribute("successMessage", "Beneficio actualizado correctamente.");
        return "redirect:/benefits";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            benefitService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Beneficio eliminado correctamente.");
        } catch (BenefitInUseException exception) {
            redirectAttributes.addFlashAttribute("errorMessage", exception.getMessage());
        }
        return "redirect:/benefits";
    }
}
