package ru.lessons.my.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
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


@Tag(name = "Vehicles", description = "Контроллер для работы с автомобилями парка")
@SecurityRequirement(name = "JWT")
@Slf4j
@RestController
@RequestMapping(value = "/api/v1/vehicles", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class VehicleRestController {

    private final VehicleService vehicleService;
    private final VehicleToVehicleDtoConverter toVehicleDtoConverter;
    private final SecurityUtils securityUtils;

    //Для тестов кэширования
    @Operation(summary = "Получить список всех автомобилей",
            description = "Метод используется для тестов")
    @GetMapping("/all")
    public List<VehicleDto> findAllVehicles() {
        List<VehicleDto> vehicles = vehicleService.findAll().stream()
                .map(toVehicleDtoConverter::convert)
                .toList();

        log.info("Found {} vehicles", vehicles.size());
        return vehicles;
    }

    @Operation(summary = "Получить список всех автомобилей залогиненного менеджера")
    @GetMapping
    public List<VehicleDto> findAllForCurrentManager() {
        Manager manager = securityUtils.getCurrentManager();

        if (manager == null) {
            return Collections.emptyList();
        }

        List<VehicleDto> vehicles = vehicleService.findByManager(manager).stream()
                .map(toVehicleDtoConverter::convert)
                .toList();

        log.info("Found {} vehicles for {}", vehicles.size(), manager.getUsername());
        return vehicles;
    }

    //todo для эскпериментов выделил версию с пагинацией в отдельный эндпоинт
    @Operation(summary = "Получить список всех автомобилей залогиненного менеджера с пагинацией")
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

    @Operation(
            summary = "Получить информацию об автомобиле по id",
            description = "Автомобиль должен быть доступен авторизованному менеджеру"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Автомобиль найден",
                    content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    array = @ArraySchema(
                            schema = @Schema(implementation = VehicleDto.class)
                    ))),
            @ApiResponse(
                    responseCode = "404",
                    description = "Автомобиль не найден",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class
                    ))),
            @ApiResponse(
                    responseCode = "401",
                    description = "Пользователь не авторизован",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class
                    ))),
            @ApiResponse(
                    responseCode = "403",
                    description = "Пользователь пытается получить доступ к автомобилю, на который у него нет прав",
                    content = @Content(
                            schema = @Schema(implementation = ErrorResponse.class
                    )))
    })
    @GetMapping("{id}")
    public ResponseEntity<?> findById(
            @PathVariable("id") @Parameter(description = "Идентификатор автомобиля", example = "1") long id) {
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

    @Operation(summary = "Создать новый автомобиль")
    @PostMapping
    public ResponseEntity<?> create(
            @Parameter(description = "VehicleDto (см. Models) в формате json переданный в теле запроса.")
            @RequestBody VehicleDto vehicleDto) {
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

    @Operation(
            summary = "Обновить данные автомобиля",
            description = "Автомобиль должен быть доступен авторизованному менеджеру"
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> update(
            @PathVariable("id") @Parameter(description = "Идентификатор автомобиля", example = "1") Long id,
            @Parameter(description = "VehicleDto (см. Models) в формате json переданный в теле запроса.")
            @RequestBody VehicleDto vehicleDto) {
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

    @Operation(
            summary = "Удалить автомобиль",
            description = "Автомобиль должен быть доступен авторизованному менеджеру"
    )
    @DeleteMapping("/{id}")
    //Зло, оставил на время для проверки
    @Transactional
    public ResponseEntity<?> delete(
            @PathVariable("id") @Parameter(description = "Идентификатор автомобиля", example = "1") long id) {
        Manager manager = securityUtils.getCurrentManager();

        Vehicle vehicle = vehicleService.findById(id);

        Optional<Enterprise> enterprise = manager.getEnterprises().stream()
                .filter(e -> e.getId().equals(vehicle.getEnterprise().getId()))
                .findFirst();

        if (enterprise.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        vehicleService.delete(vehicle);

        return ResponseEntity.noContent().build();
    }
}
