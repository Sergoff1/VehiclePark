package ru.lessons.my.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.lessons.my.converter.VehicleToVehicleDtoConverter;
import ru.lessons.my.dto.PageResult;
import ru.lessons.my.dto.VehicleDto;
import ru.lessons.my.model.Enterprise;
import ru.lessons.my.model.Manager;
import ru.lessons.my.model.Vehicle;
import ru.lessons.my.security.SecurityUtils;
import ru.lessons.my.service.VehicleService;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/v1/vehicles", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class VehicleRestController {

    private final VehicleService vehicleService;
    private final VehicleToVehicleDtoConverter toVehicleDtoConverter;
    private final SecurityUtils securityUtils;

    @GetMapping
    public List<VehicleDto> findAll() {
        Manager manager = securityUtils.getCurrentManager();

        if (manager == null) {
            return Collections.emptyList();
        }

        Set<Enterprise> enterprises = manager.getEnterprises();

        return vehicleService.findByEnterprises(enterprises).stream()
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

        return vehicleService.findByEnterprises(enterprises, page, size);
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

        return ResponseEntity.ok().body(toVehicleDtoConverter.convert(vehicleService.findById(id)));
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
