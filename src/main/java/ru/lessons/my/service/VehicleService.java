package ru.lessons.my.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import ru.lessons.my.converter.VehicleDtoToVehicleConverter;
import ru.lessons.my.dto.PageResult;
import ru.lessons.my.dto.VehicleDto;
import ru.lessons.my.model.entity.Enterprise;
import ru.lessons.my.model.entity.Manager;
import ru.lessons.my.model.entity.Vehicle;
import ru.lessons.my.repository.VehicleRepository;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final VehicleDtoToVehicleConverter toVehicleConverter;

    @Cacheable("AllVehicles")
    public List<Vehicle> findAll() {
        log.info("Find all vehicles from DB");
        return vehicleRepository.findAll();
    }

    @Cacheable("VehicleById")
    public Vehicle findById(Long id) {
        log.info("Find vehicle by id: {}", id);
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Vehicle with id %s not found", id)));
    }

    public Vehicle getByLicensePlateNumber(String licensePlateNumber) {
        return vehicleRepository.getByLicensePlateNumber(licensePlateNumber);
    }

    public List<Vehicle> findByEnterprises(Collection<Enterprise> enterprises) {
        return vehicleRepository.findByEnterprises(enterprises);
    }

    @Cacheable(value = "vehiclesByManager", key = "#p0.id")
    public List<Vehicle> findByManager(Manager manager) {
        log.info("get vehicles from DB for manager: {}", manager.getUsername());
        return vehicleRepository.findByEnterprises(manager.getEnterprises());
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
