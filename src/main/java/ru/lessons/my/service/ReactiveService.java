package ru.lessons.my.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.lessons.my.dto.GeoPointDto;
import ru.lessons.my.model.entity.GeoPoint;
import ru.lessons.my.model.entity.Trip;
import ru.lessons.my.model.entity.Vehicle;
import ru.lessons.my.repository.ReactiveGeoPointRepository;
import ru.lessons.my.repository.ReactiveTripRepository;
import ru.lessons.my.repository.ReactiveVehicleRepository;

@Service
@Slf4j
public class ReactiveService {

    private final ReactiveGeoPointRepository trackRepository;
    private final ReactiveTripRepository tripRepository;
    private final ReactiveVehicleRepository vehicleRepository;
    private final WebClient webClient;
    private final GeometryFactory geometryFactory;

    ReactiveService(ReactiveGeoPointRepository trackRepository,
                    ReactiveVehicleRepository vehicleRepository,
                    ReactiveTripRepository tripRepository) {
        this.trackRepository = trackRepository;
        this.vehicleRepository = vehicleRepository;
        this.tripRepository = tripRepository;
        this.webClient = WebClient.builder().baseUrl("http://localhost:8888").build();
        this.geometryFactory = new GeometryFactory();
    }

    @PostConstruct
    public void startConsuming() {
        consumeDataStream()
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private Flux<GeoPoint> consumeDataStream() {
        return webClient.get()
                .uri("/generator/track")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(GeoPointDto.class)
                .doOnNext(data -> log.info("Получено: {}", data))
                .flatMap(this::processAndSave)
                .doOnError(error -> log.error("Ошибка: {}", error.getMessage()))
                .retry(3);
    }

    private Mono<GeoPoint> processAndSave(GeoPointDto data) {
        //Можно сразу оперировать id и не ходить в БД, поскольку я сменил подход. Но для экспериментов пока оставлю как есть.
        Mono<Vehicle> vehicle = vehicleRepository.findById(data.getVehicleId());
        Mono<Trip> trip = tripRepository.findById(data.getTripId());
        Mono<Point> position = Mono.fromSupplier(() -> geometryFactory.createPoint(new Coordinate(data.getLongitude(), data.getLatitude())));

        return Mono.zip(vehicle, trip, position)
                .map(t -> GeoPoint.builder()
                        .visitedAt(data.getVisitedAt())
                        .vehicle(t.getT1())
                        .trip(t.getT2())
                        .position(t.getT3())
                        .build())
                .flatMap(trackRepository::save);
    }
}
