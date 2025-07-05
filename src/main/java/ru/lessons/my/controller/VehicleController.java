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
import org.springframework.web.bind.annotation.RequestParam;
import ru.lessons.my.model.Vehicle;
import ru.lessons.my.model.VehicleModel;
import ru.lessons.my.service.VehicleModelService;
import ru.lessons.my.service.VehicleService;

@Controller
@RequestMapping("/vehicles")
@RequiredArgsConstructor
public class VehicleController {

    private final VehicleService vehicleService;
    private final VehicleModelService modelService;

    @GetMapping
    public String findAllVehicles(Model model) {
        model.addAttribute("vehicles", vehicleService.findAll());
        return "vehicles/index";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("vehicle", new Vehicle());
        model.addAttribute("models", modelService.findAll());
        return "vehicles/new";
    }

    @PostMapping("/new")
    public String createVehicleModel(@Valid @ModelAttribute Vehicle vehicle,
                                     BindingResult bindingResult,
                                     @RequestParam("modelId") Long modelId) {
        if (bindingResult.hasErrors()) {
            System.out.println(bindingResult.getAllErrors());
            return "vehicles/new";
        }
        VehicleModel model = modelService.findById(modelId);
        vehicle.setModel(model);

        vehicleService.save(vehicle);
        return "redirect:/vehicles";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") long id, Model model) {
        model.addAttribute("vehicle", vehicleService.findById(id));
        model.addAttribute("models", modelService.findAll());
        return "vehicles/edit";
    }

    @PostMapping("/edit/{id}")
    public String updateVehicle(@PathVariable("id") long id,
                                @Valid @ModelAttribute Vehicle vehicle,
                                BindingResult bindingResult,
                                @RequestParam("modelId") Long modelId) {
        if (bindingResult.hasErrors()) {
            return "vehicles/edit";
        }

        VehicleModel model = modelService.findById(modelId);
        vehicle.setModel(model);
        vehicle.setId(id);
        vehicleService.save(vehicle);
        return "redirect:/vehicles";
    }

    @GetMapping("/delete/{id}")
    public String deleteVehicle(@PathVariable("id") long id) {
        vehicleService.deleteById(id);
        return "redirect:/vehicles";
    }
}
