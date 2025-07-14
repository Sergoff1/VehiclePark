package ru.lessons.my.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lessons.my.converter.DriverToDriverDtoConverter;
import ru.lessons.my.dto.DriverDto;
import ru.lessons.my.service.DriverService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/drivers", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DriverRestController {

    private final DriverService driverService;
    private final DriverToDriverDtoConverter toDriverDtoConverter;

    @GetMapping
    public List<DriverDto> findAll() {
        return driverService.findAll().stream()
                .map(toDriverDtoConverter::convert)
                .toList();
    }

    @GetMapping("{id}")
    public DriverDto findById(@PathVariable("id") long id) {
        return toDriverDtoConverter.convert(driverService.findById(id));
    }
}
