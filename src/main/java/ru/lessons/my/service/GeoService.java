package ru.lessons.my.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lessons.my.model.GeoPoint;
import ru.lessons.my.repository.GeoPointRepository;
import ru.lessons.my.repository.TripRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeoService {

    private final GeoPointRepository geoPointRepository;
    private final TripRepository tripRepository;

    public List<GeoPoint> getGeoPointsByTimeRange(long vehicleId, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return geoPointRepository.getGeoPointsByVehicleIdAndTimeRange(vehicleId, dateFrom, dateTo);
    }

    public List<GeoPoint> getGeoPointsFromTrips(long vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        return tripRepository.getGeoPointsByVehicleIdAndTimeRange(vehicleId, startDate, endDate);
    }
}
