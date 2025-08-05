package ru.lessons.my.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.lessons.my.model.VehicleModel;

import java.util.List;
import java.util.Optional;

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

    public Optional<VehicleModel> findById(Long id) {
        return Optional.ofNullable(entityManager.find(VehicleModel.class, id));
    }

    public List<VehicleModel> findAll() {
        return entityManager.createQuery("from VehicleModel", VehicleModel.class).getResultList();
    }

    @Transactional
    public void deleteById(Long id) {
        entityManager.createQuery("delete from VehicleModel where id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
