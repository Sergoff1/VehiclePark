package ru.lessons.my.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.lessons.my.model.entity.GeoPoint;
import ru.lessons.my.model.entity.Trip;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public class GeoPointRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(GeoPoint geoPoint) {
        if (geoPoint.getId() == null) {
            entityManager.persist(geoPoint);
        } else {
            entityManager.merge(geoPoint);
        }
    }

    public List<GeoPoint> getGeoPointsByVehicleIdAndTimeRange(long vehicleId, LocalDateTime from, LocalDateTime to) {
        String query = """
                SELECT g
                FROM GeoPoint g
                WHERE g.vehicle.id = :vehicleId
                    AND g.visitedAt BETWEEN :from and :to
                ORDER BY g.visitedAt
                """;
        return entityManager.createQuery(query, GeoPoint.class)
                .setParameter("vehicleId", vehicleId)
                .setParameter("from", from)
                .setParameter("to", to)
                .getResultList();
    }

    public List<GeoPoint> getGeoPointsByTrips(Collection<Trip> trips) {
        String query = "SELECT g FROM GeoPoint g WHERE g.trip IN :trips ORDER BY g.visitedAt";
        return entityManager.createQuery(query, GeoPoint.class)
                .setParameter("trips", trips)
                .getResultList();
    }
}
