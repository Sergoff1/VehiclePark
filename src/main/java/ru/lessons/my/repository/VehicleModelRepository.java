package ru.lessons.my.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.lessons.my.model.VehicleModel;

import java.util.List;

@Repository
public class VehicleModelRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void save(VehicleModel model) {
        if (model.getId() == null) {
            entityManager.persist(model);
        } else {
            entityManager.merge(model);
        }
    }

    public VehicleModel findById(Long id) {
        return entityManager.find(VehicleModel.class, id);
    }

    public List<VehicleModel> findAll() {
        return entityManager.createQuery("from VehicleModel", VehicleModel.class).getResultList();
    }

    @Transactional
    public void delete(VehicleModel model) {
        entityManager.remove(model);
    }
}
