package ru.lessons.my.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.lessons.my.converter.VehicleModelToVehicleModelDtoConverter;
import ru.lessons.my.dto.VehicleModelDto;
import ru.lessons.my.service.VehicleModelService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/models", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class VehicleModelRestController {

    private final VehicleModelService vehicleModelService;
    private final VehicleModelToVehicleModelDtoConverter modelToModelDtoConverter;

    @GetMapping
    public List<VehicleModelDto> findAll() {
        return vehicleModelService.findAll().stream()
                .map(modelToModelDtoConverter::convert)
                .toList();
    }

    @GetMapping("{id}")
    public VehicleModelDto findById(@PathVariable("id") long id) {
        return modelToModelDtoConverter.convert(vehicleModelService.findById(id));
    }

    @GetMapping("/name")
    public VehicleModelDto findByName(@RequestParam("name") String name) {
        return modelToModelDtoConverter.convert(vehicleModelService.findByName(name));
    }
}
