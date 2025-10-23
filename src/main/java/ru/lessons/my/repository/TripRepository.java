package ru.lessons.my.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.lessons.my.model.entity.Trip;

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

    public List<Trip> getTripsByVehicleIdAndTimeRange(long vehicleId, LocalDateTime startDate, LocalDateTime endDate) {
        String query = """
                SELECT t
                FROM Trip t
                WHERE t.vehicle.id = :vehicleId
                    AND t.startDate >= :startDate AND t.endDate <= :endDate
                """;

        return entityManager.createQuery(query, Trip.class)
                .setParameter("vehicleId", vehicleId)
                .setParameter("startDate", startDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }
}
