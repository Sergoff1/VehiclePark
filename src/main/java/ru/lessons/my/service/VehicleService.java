package ru.lessons.my.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lessons.my.converter.VehicleDtoToVehicleConverter;
import ru.lessons.my.dto.VehicleDto;
import ru.lessons.my.model.Enterprise;
import ru.lessons.my.model.Vehicle;
import ru.lessons.my.repository.VehicleRepository;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleDtoToVehicleConverter toVehicleConverter;

    public List<Vehicle> findAll() {
        return vehicleRepository.findAll();
    }

    public Vehicle findById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Vehicle with id %s not found", id)));
    }

    public List<Vehicle> findByEnterprises(Collection<Enterprise> enterprises) {
        return vehicleRepository.findByEnterprises(enterprises);
    }

    public void save(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
    }

    @Transactional
    public Long saveAndGetId(VehicleDto vehicleDto) {
        Vehicle vehicle = toVehicleConverter.convert(vehicleDto);
        vehicleRepository.save(vehicle);
        return vehicle.getId();
    }

    public void deleteById(Long id) {
        vehicleRepository.deleteById(id);
    }
}
