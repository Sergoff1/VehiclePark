package ru.lessons.my.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;
import ru.lessons.my.model.Vehicle;

import java.util.List;

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

    public Vehicle findById(Long id) {
        return entityManager.find(Vehicle.class, id);
    }

    public List<Vehicle> findAll() {
        return entityManager.createQuery("from Vehicle", Vehicle.class).getResultList();
    }

    @Transactional
    public void deleteById(Long id) {
        entityManager.createQuery("delete from Vehicle where id = :id")
                .setParameter("id", id)
                .executeUpdate();
    }
}
