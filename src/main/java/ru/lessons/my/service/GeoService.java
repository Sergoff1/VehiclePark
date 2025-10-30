package ru.lessons.my.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lessons.my.model.entity.GeoPoint;
import ru.lessons.my.model.entity.Trip;
import ru.lessons.my.repository.GeoPointRepository;
import ru.lessons.my.repository.TripRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GeoService {

    private final GeoPointRepository geoPointRepository;
    private final TripRepository tripRepository;

    public List<GeoPoint> getGeoPointsByTimeRange(long vehicleId, LocalDateTime dateFrom, LocalDateTime dateTo) {
        return geoPointRepository.getGeoPointsByVehicleIdAndTimeRange(vehicleId, dateFrom, dateTo);
    }

    public List<GeoPoint> getGeoPointsByTrips(Collection<Trip> trips) {
        return geoPointRepository.getGeoPointsByTrips(trips);
    }

    public List<Trip> getTripsByVehicleIdAndTimeRange(long vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        return tripRepository.getTripsByVehicleIdAndTimeRange(vehicleId, startDate, endDate);
    }

    public List<Trip> getTripsByEnterpriseIdAndTimeRange(long enterpriseId, LocalDateTime startDate, LocalDateTime endDate) {
        return tripRepository.getTripsByEnterpriseIdAndTimeRange(enterpriseId, startDate, endDate);
    }

    public boolean isTripDatesOverlapExisting(long vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        return tripRepository.isTripDatesOverlapExisting(vehicleId, startDate, endDate);
    }

    public void save(Trip trip) {
        tripRepository.save(trip);
    }

    public void save(GeoPoint point) {
        geoPointRepository.save(point);
    }
}
