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
        hints.put("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Vehicle.detail"));

        return Optional.ofNullable(entityManager.find(Vehicle.class, id, hints));
    }

    public List<Vehicle> findAll() {
        TypedQuery<Vehicle> query = entityManager.createQuery("SELECT v FROM Vehicle v", Vehicle.class);
        query.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Vehicle.detail"));

        return query.getResultList();
    }

    public List<Vehicle> findByEnterprises(Collection<Enterprise> enterprises) {
        if (enterprises == null || enterprises.isEmpty()) {
            return Collections.emptyList();
        }
        TypedQuery<Vehicle> query = entityManager.createQuery(
                "SELECT v FROM Vehicle v WHERE v.enterprise IN :enterprises", Vehicle.class);
        query.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Vehicle.detail"));
        query.setParameter("enterprises", enterprises);
        return query.getResultList();
    }

    public List<Vehicle> findByEnterprises(Collection<Enterprise> enterprises, int page, int size) {
        if (enterprises == null || enterprises.isEmpty()) {
            return Collections.emptyList();
        }
        TypedQuery<Vehicle> query = entityManager.createQuery(
                "SELECT v FROM Vehicle v WHERE v.enterprise IN :enterprises ORDER BY v.color, v.id", Vehicle.class);
        query.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Vehicle.detail"));
        query.setParameter("enterprises", enterprises);
        query.setFirstResult((page - 1) * size);
        query.setMaxResults(size);
        return query.getResultList();
    }

    public long countVehiclesByEnterprises(Collection<Enterprise> enterprises) {
        String jpql = "SELECT COUNT(v) FROM Vehicle v WHERE v.enterprise IN :enterprises";
        return entityManager.createQuery(jpql, Long.class)
                .setParameter("enterprises", enterprises)
                .getSingleResult();
    }

    @Transactional
    public void deleteById(Long id) {
        entityManager.createQuery("delete from Vehicle where id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
