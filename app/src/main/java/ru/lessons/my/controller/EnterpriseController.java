package ru.lessons.my.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.lessons.my.model.entity.Enterprise;
import ru.lessons.my.model.entity.Manager;
import ru.lessons.my.security.SecurityUtils;
import ru.lessons.my.service.EnterpriseService;

import java.time.ZoneId;
import java.util.List;

@Controller
@RequestMapping("/enterprises")
@RequiredArgsConstructor
public class EnterpriseController {

    private final EnterpriseService enterpriseService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String findAll(Model model) {
        Manager manager = securityUtils.getCurrentManager();

        model.addAttribute("enterprises", enterpriseService.findByManager(manager));
        return "enterprises/index";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") long id, Model model) {
        //todo Проверка, что менеджер имеет права на изменение этого предприятия
        model.addAttribute("enterprise", enterpriseService.findById(id));

        List<String> timeZones = ZoneId.getAvailableZoneIds().stream().sorted().toList();
        model.addAttribute("timeZones", timeZones);
        return "enterprises/edit";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable("id") long id,
                         @Valid @ModelAttribute Enterprise enterprise,
                         BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "enterprises/edit";
        }

        enterprise.setId(id);
        enterpriseService.save(enterprise);
        return "redirect:/enterprises";
    }
}
