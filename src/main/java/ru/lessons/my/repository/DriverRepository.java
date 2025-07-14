package ru.lessons.my.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.lessons.my.model.Driver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Driver findById(Long id) {
        Map<String, Object> hints = new HashMap<>();
        hints.put("javax.persistence.fetchgraph", entityManager.getEntityGraph("Driver.detail"));

        return entityManager.find(Driver.class, id, hints);
    }

    public List<Driver> findAll() {
        TypedQuery<Driver> query = entityManager.createQuery("SELECT d FROM Driver d", Driver.class);
        query.setHint("javax.persistence.fetchgraph", entityManager.getEntityGraph("Driver.detail"));

        return query.getResultList();
    }

    @Transactional
    public void deleteById(Long id) {
        entityManager.createQuery("delete from Driver where id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
