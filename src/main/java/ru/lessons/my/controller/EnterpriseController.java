package ru.lessons.my.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.lessons.my.model.Manager;
import ru.lessons.my.security.SecurityUtils;
import ru.lessons.my.service.EnterpriseService;

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
}
