package ru.lessons.my.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.lessons.my.model.entity.VehicleModel;
import ru.lessons.my.repository.VehicleModelRepository;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleModelService {

    private final VehicleModelRepository vehicleModelRepository;

    public List<VehicleModel> findAll() {
        return vehicleModelRepository.findAll();
    }

    public VehicleModel findById(Long id) {
        return vehicleModelRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Vehicle Model with id %s not found", id)));
    }

    @Cacheable("vehicleModelByName")
    public VehicleModel findByName(String name) {
        log.info("get vehicle model by name '{}' from DB", name);
        return vehicleModelRepository.findByModelName(name)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Vehicle Model with model name %s not found", name)));
    }

    public void save(VehicleModel vehicleModel) {
        vehicleModelRepository.save(vehicleModel);
    }

    public void deleteById(Long id) {
        vehicleModelRepository.deleteById(id);
    }
}
