package ru.lessons.my.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.lessons.my.service.VehicleModelService;

@Controller
@RequestMapping("/models")
@RequiredArgsConstructor
public class VehicleModelController {

    private final VehicleModelService vehicleModelService;

    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("vehicleModels", vehicleModelService.findAll());
        return "vehicleModels";
    }
}
