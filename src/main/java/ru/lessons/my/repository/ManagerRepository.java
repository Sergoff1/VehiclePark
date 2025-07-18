package ru.lessons.my.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.lessons.my.model.Manager;

@Repository
public class ManagerRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(Manager manager) {
        if (manager.getId() == null) {
            entityManager.persist(manager);
        } else {
            entityManager.merge(manager);
        }
    }

    public Manager getByUsername(String username) {
        String query = "SELECT m FROM Manager m WHERE m.username = :username";
        return entityManager.createQuery(query, Manager.class)
                .setParameter("username", username)
                .getSingleResult();
    }
}
