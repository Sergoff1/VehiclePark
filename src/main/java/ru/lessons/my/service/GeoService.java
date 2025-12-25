package ru.lessons.my.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.lessons.my.bot.TelegramBot;
import ru.lessons.my.model.entity.GeoPoint;
import ru.lessons.my.model.entity.Manager;
import ru.lessons.my.model.entity.Trip;
import ru.lessons.my.repository.GeoPointRepository;
import ru.lessons.my.repository.TripRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeoService {

    private final GeoPointRepository geoPointRepository;
    private final TripRepository tripRepository;
    private final TelegramBot telegramBot;

    @Cacheable("geoPointsByTrips")
    public List<GeoPoint> getGeoPointsByTimeRange(long vehicleId, LocalDateTime dateFrom, LocalDateTime dateTo) {
        log.info("getGeoPoints from DB");
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
        Set<Long> managerIds = trip.getVehicle().getEnterprise().getManagers().stream()
                .map(Manager::getId)
                .collect(Collectors.toSet());

        //todo Циклическая зависимость. Исправить, когда перейдём на работу с сообщениями
        telegramBot.notifyAboutTrip(managerIds, trip.getVehicle().getEnterprise().getName());
    }

    public void save(GeoPoint point) {
        geoPointRepository.save(point);
    }
}
