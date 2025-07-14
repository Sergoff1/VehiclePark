package ru.lessons.my.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.lessons.my.converter.EnterpriseToEnterpriseDtoConverter;
import ru.lessons.my.dto.EnterpriseDto;
import ru.lessons.my.service.EnterpriseService;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/enterprises", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EnterpriseRestController {

    private final EnterpriseService enterpriseService;
    private final EnterpriseToEnterpriseDtoConverter toEnterpriseDtoConverter;

    @GetMapping
    public List<EnterpriseDto> findAll() {
        return enterpriseService.findAll().stream()
                .map(toEnterpriseDtoConverter::convert)
                .toList();
    }

    @GetMapping("{id}")
    public EnterpriseDto findById(@PathVariable("id") long id) {
        return toEnterpriseDtoConverter.convert(enterpriseService.findById(id));
    }
}
