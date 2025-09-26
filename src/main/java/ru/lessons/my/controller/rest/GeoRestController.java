package ru.lessons.my.controller.rest;

import lombok.RequiredArgsConstructor;
import org.geojson.Feature;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.lessons.my.converter.GeoPointToFeatureConverter;
import ru.lessons.my.converter.GeoPointToGeoPointDtoConverter;
import ru.lessons.my.dto.GeoPointDto;
import ru.lessons.my.model.GeoPoint;
import ru.lessons.my.model.Vehicle;
import ru.lessons.my.service.GeoService;
import ru.lessons.my.service.VehicleService;
import ru.lessons.my.util.DateTimeUtils;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/v1/geo")
@RequiredArgsConstructor
public class GeoRestController {

    private final GeoService geoService;
    private final VehicleService vehicleService;
    private final GeoPointToFeatureConverter toFeatureConverter;
    private final GeoPointToGeoPointDtoConverter toGeoPointDtoConverter;

    @RequestMapping(value = "/json/track")
    public List<GeoPointDto> getJsonTrack(@RequestParam("vehicleId") long vehicleId,
                                         @RequestParam("dateFrom") LocalDateTime dateFrom,
                                         @RequestParam("dateTo") LocalDateTime dateTo) {

        return getTrackInternal(vehicleId, dateFrom, dateTo).stream()
                .map(toGeoPointDtoConverter::convert)
                .toList();
    }

    @RequestMapping(value = "/geojson/track")
    public List<Feature> getGeoJsonTrack(@RequestParam("vehicleId") long vehicleId,
                                         @RequestParam("dateFrom") LocalDateTime dateFrom,
                                         @RequestParam("dateTo") LocalDateTime dateTo) {

        return getTrackInternal(vehicleId, dateFrom, dateTo).stream()
                .map(toFeatureConverter::convert)
                .toList();
    }

    private List<GeoPoint> getTrackInternal(long vehicleId, LocalDateTime from, LocalDateTime to) {
        Vehicle vehicle = vehicleService.findById(vehicleId);

        ZoneId enterpriseTimeZone = ZoneId.of(vehicle.getEnterprise().getTimeZone());

        LocalDateTime utcFrom = DateTimeUtils.convertToUtc(from, enterpriseTimeZone);
        LocalDateTime utcTo = DateTimeUtils.convertToUtc(to, enterpriseTimeZone);

        return geoService.getGeoPointsByCoordinates(vehicleId, utcFrom, utcTo);
    }

}
