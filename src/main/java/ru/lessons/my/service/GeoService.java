package ru.lessons.my.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lessons.my.model.GeoPoint;
import ru.lessons.my.repository.GeoPointRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeoService {

    private final GeoPointRepository geoPointRepository;

    public List<GeoPoint> getGeoPointsByCoordinates(long vehicleId, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return geoPointRepository.getGeoPointsByVehicleIdAndTimeRange(vehicleId, dateFrom, dateTo);
    }
}
