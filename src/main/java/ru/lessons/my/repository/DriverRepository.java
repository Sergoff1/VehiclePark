package ru.lessons.my.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.lessons.my.model.entity.Driver;
import ru.lessons.my.model.entity.Enterprise;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class DriverRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(Driver driver) {
        if (driver.getId() == null) {
            entityManager.persist(driver);
        } else {
            entityManager.merge(driver);
        }
    }

    public Optional<Driver> findById(Long id) {
        Map<String, Object> hints = new HashMap<>();
        hints.put("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Driver.detail"));

        return Optional.ofNullable(entityManager.find(Driver.class, id, hints));
    }

    public List<Driver> findAll() {
        TypedQuery<Driver> query = entityManager.createQuery("SELECT d FROM Driver d", Driver.class);
        query.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Driver.detail"));

        return query.getResultList();
    }

    public List<Driver> findByEnterprises(Collection<Enterprise> enterprises) {
        if (enterprises == null || enterprises.isEmpty()) {
            return Collections.emptyList();
        }
        TypedQuery<Driver> query = entityManager.createQuery(
                "SELECT d FROM Driver d WHERE d.enterprise IN :enterprises", Driver.class);
        query.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Driver.detail"));
        query.setParameter("enterprises", enterprises);
        return query.getResultList();
    }

    public List<Driver> findByEnterpriseId(Long enterpriseId) {
        TypedQuery<Driver> query = entityManager.createQuery(
                "SELECT d FROM Driver d WHERE d.enterprise.id = :enterpriseId", Driver.class);
        query.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Driver.detail"));
        query.setParameter("enterpriseId", enterpriseId);
        return query.getResultList();
    }

    @Transactional
    public void deleteById(Long id) {
        entityManager.createQuery("delete from Driver where id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
