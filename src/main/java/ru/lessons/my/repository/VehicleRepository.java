package ru.lessons.my.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.lessons.my.model.Enterprise;
import ru.lessons.my.model.Vehicle;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class VehicleRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(Vehicle vehicle) {
        if (vehicle.getId() == null) {
            entityManager.persist(vehicle);
        } else {
            entityManager.merge(vehicle);
        }
    }

    public Optional<Vehicle> findById(Long id) {
        Map<String, Object> hints = new HashMap<>();
        hints.put("javax.persistence.fetchgraph", entityManager.getEntityGraph("Vehicle.detail"));

        return Optional.ofNullable(entityManager.find(Vehicle.class, id, hints));
    }

    public List<Vehicle> findAll() {
        TypedQuery<Vehicle> query = entityManager.createQuery("SELECT v FROM Vehicle v", Vehicle.class);
        query.setHint("javax.persistence.fetchgraph", entityManager.getEntityGraph("Vehicle.detail"));

        return query.getResultList();
    }

    public List<Vehicle> findByEnterprises(Collection<Enterprise> enterprises) {
        if (enterprises == null || enterprises.isEmpty()) {
            return Collections.emptyList();
        }
        TypedQuery<Vehicle> query = entityManager.createQuery(
                "SELECT v FROM Vehicle v WHERE v.enterprise IN :enterprises", Vehicle.class);
        query.setHint("javax.persistence.fetchgraph", entityManager.getEntityGraph("Vehicle.detail"));
        query.setParameter("enterprises", enterprises);
        return query.getResultList();
    }

    @Transactional
    public void deleteById(Long id) {
        entityManager.createQuery("delete from Vehicle where id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
