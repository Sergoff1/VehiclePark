package ru.lessons.my.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import ru.lessons.my.model.GeoPoint;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class GeoPointRepository {

    @PersistenceContext
    private EntityManager entityManager;

    public List<GeoPoint> getGeoPointsByVehicleIdAndTimeRange(long vehicleId, LocalDateTime from, LocalDateTime to) {
        String query = "SELECT g FROM GeoPoint g WHERE g.vehicle.id = :vehicleId AND g.visitedAt BETWEEN :from and :to";
        return entityManager.createQuery(query, GeoPoint.class)
                .setParameter("vehicleId", vehicleId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }
}
