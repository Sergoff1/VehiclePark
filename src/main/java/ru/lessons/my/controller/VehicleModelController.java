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
import ru.lessons.my.model.VehicleModel;
import ru.lessons.my.service.VehicleModelService;

@Controller
@RequestMapping("/models")
@RequiredArgsConstructor
public class VehicleModelController {

    private final VehicleModelService vehicleModelService;

    @GetMapping
    public String findAll(Model model) {
        model.addAttribute("vehicleModels", vehicleModelService.findAll());
        return "models/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("vehicleModel", new VehicleModel());
        return "models/new";
    }

    @PostMapping("/new")
    public String createVehicleModel(@Valid @ModelAttribute VehicleModel vehicleModel, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "models/new";
        }
        vehicleModelService.save(vehicleModel);
        return "redirect:/models";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") long id, Model model) {
        VehicleModel vehicleModel = vehicleModelService.findById(id);
        model.addAttribute("vehicleModel", vehicleModel);
        return "models/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateVehicleModel(@PathVariable("id") long id,
                                     @Valid @ModelAttribute VehicleModel vehicleModel,
                                     BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "models/edit";
        }

        vehicleModel.setId(id);
        vehicleModelService.save(vehicleModel);
        return "redirect:/models";
    }

    @GetMapping("/delete/{id}")
    public String deleteVehicleModel(@PathVariable("id") long id) {
        vehicleModelService.deleteById(id);
        return "redirect:/models";
    }

}
