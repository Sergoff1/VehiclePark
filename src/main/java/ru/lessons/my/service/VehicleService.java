package ru.lessons.my.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lessons.my.model.Enterprise;
import ru.lessons.my.model.Vehicle;
import ru.lessons.my.repository.VehicleRepository;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id);
    }

    public List<Vehicle> findByEnterprises(Collection<Enterprise> enterprises) {
        return vehicleRepository.findByEnterprises(enterprises);
    }

    public void save(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
    }

    public void deleteById(Long id) {
        vehicleRepository.deleteById(id);
    }
}
