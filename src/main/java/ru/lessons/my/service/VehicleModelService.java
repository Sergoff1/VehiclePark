package ru.lessons.my.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lessons.my.model.VehicleModel;
import ru.lessons.my.repository.VehicleModelRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleModelService {

    private final VehicleModelRepository vehicleModelRepository;

    public List<VehicleModel> findAll() {
        return vehicleModelRepository.findAll();
    }

    public VehicleModel findById(Long id) {
        return vehicleModelRepository.findById(id);
    }

    public void save(VehicleModel vehicleModel) {
        vehicleModelRepository.save(vehicleModel);
    }

    public void deleteById(Long id) {
        vehicleModelRepository.deleteById(id);
    }
}
