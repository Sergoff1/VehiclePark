package ru.lessons.my.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.lessons.my.model.GeoPoint;
import ru.lessons.my.model.Trip;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class TripRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(Trip trip) {
        if (trip.getId() == null) {
            entityManager.persist(trip);
        } else {
            entityManager.merge(trip);
        }
    }

    public List<GeoPoint> getGeoPointsByVehicleIdAndTimeRange(long vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        String query = """
                SELECT g FROM GeoPoint g
                JOIN Trip t ON g.vehicle.id = t.vehicle.id
                WHERE g.vehicle.id = :vehicleId
                    AND t.startDate >= :startDate AND t.endDate <= :endDate
                    AND g.visitedAt BETWEEN t.startDate AND t.endDate
                """;

        return entityManager.createQuery(query, GeoPoint.class)
                .setParameter("vehicleId", vehicleId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }
}
