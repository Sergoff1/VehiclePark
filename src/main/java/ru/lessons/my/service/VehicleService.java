package ru.lessons.my.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lessons.my.converter.VehicleDtoToVehicleConverter;
import ru.lessons.my.converter.VehicleToVehicleDtoConverter;
import ru.lessons.my.dto.PageResult;
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
    private final VehicleToVehicleDtoConverter toVehicleDtoConverter;

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

    //todo подумать над кастомизацией фильтрации
    public PageResult<Vehicle> findByEnterprises(Collection<Enterprise> enterprises, int page, int size) {
         List<Vehicle> vehicles = vehicleRepository.findByEnterprises(enterprises, page, size);
         long vehicleCount = vehicleRepository.countVehiclesByEnterprises(enterprises);

         return PageResult.<Vehicle>builder()
                 .page(page)
                 .size(size)
                 .totalPages(Math.ceilDiv(vehicleCount, size))
                 .totalElements(vehicleCount)
                 .content(vehicles)
                 .build();
    }

    public void save(Vehicle vehicle) {
        vehicleRepository.save(vehicle);
    }

    @Transactional
    public Vehicle saveAndGet(VehicleDto vehicleDto) {
        Vehicle vehicle = toVehicleConverter.convert(vehicleDto);
        vehicleRepository.save(vehicle);
        return vehicle;
    }

    public void deleteById(Long id) {
        vehicleRepository.deleteById(id);
    }
}
