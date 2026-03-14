package ru.lessons.my.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.lessons.my.model.entity.Enterprise;
import ru.lessons.my.model.entity.Manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public class EnterpriseRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(Enterprise enterprise) {
        if (enterprise.getId() == null) {
            entityManager.persist(enterprise);
        } else {
            entityManager.merge(enterprise);
        }
    }

    public Optional<Enterprise> findById(Long id) {
        Map<String, Object> hints = new HashMap<>();
        hints.put("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Enterprise.detail"));

        return Optional.ofNullable(entityManager.find(Enterprise.class, id, hints));
    }

    //todo Ненадёжно (регистр, исключения, одинаковые имена?)
    public Enterprise getByName(String name) {
        TypedQuery<Enterprise> query = entityManager.createQuery("SELECT e FROM Enterprise e WHERE e.name = :name", Enterprise.class);
        query.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Enterprise.detail"));
        query.setParameter("name", name);

        return query.getSingleResult();
    }

    public List<Enterprise> findAll() {
        TypedQuery<Enterprise> query = entityManager.createQuery("SELECT e FROM Enterprise e", Enterprise.class);
        query.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Enterprise.detail"));

        return query.getResultList();
    }

    public List<Enterprise> findByManager(Manager manager) {
        TypedQuery<Enterprise> query = entityManager.createQuery(
                "SELECT e FROM Enterprise e WHERE :manager member of e.managers", Enterprise.class);
        query.setHint("jakarta.persistence.fetchgraph", entityManager.getEntityGraph("Enterprise.detail"));
        query.setParameter("manager", manager);
        return query.getResultList();
    }

    @Transactional
    public void deleteById(Long id) {
        entityManager.createQuery("delete from Enterprise where id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }

}
