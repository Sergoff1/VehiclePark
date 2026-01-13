package ru.lessons.my.controller.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.lessons.my.converter.VehicleToVehicleDtoConverter;
import ru.lessons.my.dto.PageResult;
import ru.lessons.my.dto.VehicleDto;
import ru.lessons.my.model.entity.Enterprise;
import ru.lessons.my.model.entity.Manager;
import ru.lessons.my.model.entity.Vehicle;
import ru.lessons.my.security.SecurityUtils;
import ru.lessons.my.service.VehicleService;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping(value = "/api/v1/vehicles", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class VehicleRestController {

    private final VehicleService vehicleService;
    private final VehicleToVehicleDtoConverter toVehicleDtoConverter;
    private final SecurityUtils securityUtils;

    //Для тестов кэширования
    @GetMapping("/all")
    public List<VehicleDto> findAllVehicles() {
        return vehicleService.findAll().stream()
                .map(toVehicleDtoConverter::convert)
                .toList();
    }

    @GetMapping
    public List<VehicleDto> findAllForCurrentManager() {
        Manager manager = securityUtils.getCurrentManager();

        if (manager == null) {
            return Collections.emptyList();
        }

        return vehicleService.findByManager(manager).stream()
                .map(toVehicleDtoConverter::convert)
                .toList();
    }

    //todo для эскпериментов выделил версию с пагинацией в отдельный эндпоинт
    @GetMapping("paged")
    public PageResult<VehicleDto> findAllPaginated(@RequestParam(defaultValue = "1", name = "page") int page,
                                                   @RequestParam(defaultValue = "20", name = "size") int size) {
        Manager manager = securityUtils.getCurrentManager();

        if (manager == null) {
            return null;
        }

        Set<Enterprise> enterprises = manager.getEnterprises();
        //Сделал какой-то ужас, нужно привести это к нормальному виду.
        PageResult<Vehicle> intermediateResult = vehicleService.findByEnterprises(enterprises, page, size);

        return PageResult.<VehicleDto>builder()
                .page(intermediateResult.getPage())
                .size(intermediateResult.getSize())
                .totalPages(intermediateResult.getTotalPages())
                .totalElements(intermediateResult.getTotalElements())
                .content(intermediateResult.getContent().stream().map(toVehicleDtoConverter::convert).toList())
                .build();
    }

    @GetMapping("{id}")
    public ResponseEntity<?> findById(@PathVariable("id") long id) {
        Manager manager = securityUtils.getCurrentManager();

        Vehicle vehicle = vehicleService.findById(id);

        Optional<Enterprise> enterprise = manager.getEnterprises().stream()
                .filter(e -> e.getId().equals(vehicle.getEnterprise().getId()))
                .findFirst();

        if (enterprise.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Нельзя просматривать автомобили стороннего предприятия");
        }

        return ResponseEntity.ok().body(toVehicleDtoConverter.convert(vehicle));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody VehicleDto vehicleDto) {
        Manager manager = securityUtils.getCurrentManager();

        Optional<Enterprise> enterprise = manager.getEnterprises().stream()
                .filter(e -> e.getId().equals(vehicleDto.getEnterpriseId()))
                .findFirst();

        if (enterprise.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Нельзя создать автомобиль для стороннего предприятия");
        }

        Vehicle newVehicle = vehicleService.saveAndGet(vehicleDto);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newVehicle.getId())
                .toUri();
        return ResponseEntity.created(location).body(toVehicleDtoConverter.convert(newVehicle));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody VehicleDto vehicleDto) {
        Manager manager = securityUtils.getCurrentManager();

        Vehicle vehicle = vehicleService.findById(id);

        Optional<Enterprise> enterprise = manager.getEnterprises().stream()
                .filter(e -> e.getId().equals(vehicle.getEnterprise().getId()))
                .findFirst();

        if (enterprise.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Нельзя редактировать автомобиль стороннего предприятия");
        }

        //Костыль, чтобы юзер не заменил id машины.
        // todo Перейти на более изящное решение.
        vehicleDto.setId(id);
        vehicleService.saveAndGet(vehicleDto);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") long id) {
        Manager manager = securityUtils.getCurrentManager();

        Vehicle vehicle = vehicleService.findById(id);

        Optional<Enterprise> enterprise = manager.getEnterprises().stream()
                .filter(e -> e.getId().equals(vehicle.getEnterprise().getId()))
                .findFirst();

        if (enterprise.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        vehicleService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
