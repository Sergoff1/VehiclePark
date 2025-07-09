package ru.lessons.my.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lessons.my.converter.VehicleToVehicleDtoConverter;
import ru.lessons.my.dto.VehicleDto;
import ru.lessons.my.service.VehicleService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/vehicles", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class VehicleRestController {

    private final VehicleService vehicleService;
    private final VehicleToVehicleDtoConverter toVehicleDtoConverter;

    @GetMapping
    public List<VehicleDto> findAll() {
        return vehicleService.findAll().stream()
                .map(toVehicleDtoConverter::convert)
                .toList();
    }

    @GetMapping("{id}")
    public VehicleDto findById(@PathVariable("id") long id) {
        return toVehicleDtoConverter.convert(vehicleService.findById(id));
    }
}
