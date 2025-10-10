package ru.lessons.my.controller.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.lessons.my.converter.GeoPointToFeatureConverter;
import ru.lessons.my.converter.GeoPointToGeoPointDtoConverter;
import ru.lessons.my.converter.TripToTripDtoConverter;
import ru.lessons.my.dto.TripDto;
import ru.lessons.my.model.GeoPoint;
import ru.lessons.my.model.Trip;
import ru.lessons.my.model.Vehicle;
import ru.lessons.my.service.GeoService;
import ru.lessons.my.service.VehicleService;
import ru.lessons.my.util.DateTimeUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@RestController
@RequestMapping(value = "/api/v1")
@RequiredArgsConstructor
public class GeoRestController {

    private final GeoService geoService;
    private final VehicleService vehicleService;
    private final GeoPointToFeatureConverter toFeatureConverter;
    private final GeoPointToGeoPointDtoConverter toGeoPointDtoConverter;
    private final TripToTripDtoConverter toTripDtoConverter;

    @GetMapping("/tracks")
    public List<?> getTrack(@RequestParam("vehicleId") long vehicleId,
                            @RequestParam("dateFrom") LocalDateTime dateFrom,
                            @RequestParam("dateTo") LocalDateTime dateTo,
                            @RequestParam(name = "format", defaultValue = "json") String format) {

        Vehicle vehicle = vehicleService.findById(vehicleId);

        ZoneId enterpriseTimeZone = ZoneId.of(vehicle.getEnterprise().getTimeZone());

        LocalDateTime utcFrom = DateTimeUtils.convertToUtc(dateFrom, enterpriseTimeZone);
        LocalDateTime utcTo = DateTimeUtils.convertToUtc(dateTo, enterpriseTimeZone);

        List<Trip> trips = geoService.getTripsByVehicleIdAndTimeRange(vehicleId, utcFrom, utcTo);
        List<GeoPoint> tracks = geoService.getGeoPointsByTrips(trips);

        return "geojson".equalsIgnoreCase(format)
                ? tracks.stream().map(toFeatureConverter::convert).toList()
                : tracks.stream().map(toGeoPointDtoConverter::convert).toList();
    }

    @GetMapping("/trips")
    public List<TripDto> getTrips(@RequestParam("vehicleId") long vehicleId,
                            @RequestParam("dateFrom") LocalDateTime dateFrom,
                            @RequestParam("dateTo") LocalDateTime dateTo) {

        Vehicle vehicle = vehicleService.findById(vehicleId);

        ZoneId enterpriseTimeZone = ZoneId.of(vehicle.getEnterprise().getTimeZone());

        LocalDateTime utcFrom = DateTimeUtils.convertToUtc(dateFrom, enterpriseTimeZone);
        LocalDateTime utcTo = DateTimeUtils.convertToUtc(dateTo, enterpriseTimeZone);

        List<Trip> trips = geoService.getTripsByVehicleIdAndTimeRange(vehicleId, utcFrom, utcTo);

        return trips.stream().map(toTripDtoConverter::convert).toList();
    }

}
