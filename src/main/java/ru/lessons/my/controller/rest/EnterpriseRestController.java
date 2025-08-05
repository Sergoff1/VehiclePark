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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import ru.lessons.my.converter.EnterpriseDtoToEnterpriseConverter;
import ru.lessons.my.converter.EnterpriseToEnterpriseDtoConverter;
import ru.lessons.my.dto.EnterpriseDto;
import ru.lessons.my.model.Enterprise;
import ru.lessons.my.model.Manager;
import ru.lessons.my.security.SecurityUtils;
import ru.lessons.my.service.EnterpriseService;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(value = "/api/v1/enterprises", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class EnterpriseRestController {

    private final EnterpriseService enterpriseService;
    private final EnterpriseToEnterpriseDtoConverter toEnterpriseDtoConverter;
    private final EnterpriseDtoToEnterpriseConverter toEnterpriseConverter;
    private final SecurityUtils securityUtils;

    //todo Логику в сервис
    @GetMapping
    public List<EnterpriseDto> findAll() {
        Manager manager = securityUtils.getCurrentManager();

        return enterpriseService.findByManager(manager).stream()
                .map(toEnterpriseDtoConverter::convert)
                .toList();
    }

    @GetMapping("{id}")
    public ResponseEntity<?> findById(@PathVariable("id") long id) {
        Manager manager = securityUtils.getCurrentManager();

        Optional<Enterprise> enterprise = manager.getEnterprises().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();

        if (enterprise.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok().body(toEnterpriseDtoConverter.convert(enterpriseService.findById(id)));
    }

    //todo Выделить логику валидации в отдельное место?
    // Передавать дто в сервис и там выполнять конвертацию.
    @PostMapping
    public ResponseEntity<?> create(@RequestBody EnterpriseDto enterpriseDto) {
        Manager manager = securityUtils.getCurrentManager();
        Enterprise newEnterprise = toEnterpriseConverter.convert(enterpriseDto);

        enterpriseService.saveWithManager(newEnterprise, manager);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newEnterprise.getId())
                .toUri();
        return ResponseEntity.created(location).body("Enterprise with id " + newEnterprise.getId() + " created");
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable("id") Long id, @RequestBody EnterpriseDto enterpriseDto) {
        Manager manager = securityUtils.getCurrentManager();

        Optional<Enterprise> enterprise = manager.getEnterprises().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();

        if (enterprise.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        //Костыль, чтобы юзер не заменил id.
        enterpriseDto.setId(id);
        enterpriseService.update(enterpriseDto);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable("id") Long id) {
        Manager manager = securityUtils.getCurrentManager();

        Optional<Enterprise> enterprise = manager.getEnterprises().stream()
                .filter(e -> e.getId().equals(id))
                .findFirst();

        if (enterprise.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        enterpriseService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
