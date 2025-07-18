package ru.lessons.my.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lessons.my.converter.DriverToDriverDtoConverter;
import ru.lessons.my.dto.DriverDto;
import ru.lessons.my.model.Enterprise;
import ru.lessons.my.model.Manager;
import ru.lessons.my.service.DriverService;
import ru.lessons.my.service.ManagerService;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(value = "/api/v1/drivers", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DriverRestController {

    private final DriverService driverService;
    private final DriverToDriverDtoConverter toDriverDtoConverter;
    private final ManagerService managerService;

    @GetMapping
    public List<DriverDto> findAll(Authentication authentication) {
        Manager manager = managerService.getManagerByUsername(authentication.getName());

        if (manager == null) {
            return Collections.emptyList();
        }

        Set<Enterprise> enterprises = manager.getEnterprises();

        return driverService.findByEnterprises(enterprises).stream()
                .map(toDriverDtoConverter::convert)
                .toList();
    }

    @GetMapping("{id}")
    public DriverDto findById(@PathVariable("id") long id) {
        return toDriverDtoConverter.convert(driverService.findById(id));
    }
}
