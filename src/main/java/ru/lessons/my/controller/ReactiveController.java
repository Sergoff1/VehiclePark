package ru.lessons.my.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import ru.lessons.my.converter.VehicleToVehicleDtoConverter;
import ru.lessons.my.dto.VehicleDto;
import ru.lessons.my.service.VehicleService;

import java.time.Duration;

@RestController
@Slf4j
@RequestMapping("/reactive/")
@RequiredArgsConstructor
public class ReactiveController {

    private final VehicleService vehicleService;
    private final VehicleToVehicleDtoConverter toVehicleDtoConverter;

    @GetMapping(value = "/vehicles", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<VehicleDto> vehiclesFlux() {
        return Flux.defer(() -> Flux.fromIterable(vehicleService.findAll()))
                .map(toVehicleDtoConverter::convert)
                .delayElements(Duration.ofMillis(500))
                .subscribeOn(Schedulers.boundedElastic());
    }

}
