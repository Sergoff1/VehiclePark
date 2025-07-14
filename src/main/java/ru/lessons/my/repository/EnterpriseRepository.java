package ru.lessons.my.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.lessons.my.model.Enterprise;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public Enterprise findById(Long id) {
        Map<String, Object> hints = new HashMap<>();
        hints.put("javax.persistence.fetchgraph", entityManager.getEntityGraph("Enterprise.detail"));

        return entityManager.find(Enterprise.class, id, hints);
    }

    public List<Enterprise> findAll() {
        TypedQuery<Enterprise> query = entityManager.createQuery("SELECT e FROM Enterprise e", Enterprise.class);
        query.setHint("javax.persistence.fetchgraph", entityManager.getEntityGraph("Enterprise.detail"));

        return query.getResultList();
    }

    @Transactional
    public void deleteById(Long id) {
        entityManager.createQuery("delete from Enterprise where id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
