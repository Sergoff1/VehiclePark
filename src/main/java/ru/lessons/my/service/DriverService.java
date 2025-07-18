package ru.lessons.my.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lessons.my.model.Driver;
import ru.lessons.my.model.Enterprise;
import ru.lessons.my.repository.DriverRepository;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    public List<Driver> findAll() {
        return driverRepository.findAll();
    }

    public Driver findById(Long id) {
        return driverRepository.findById(id);
    }

    public List<Driver> findByEnterprises(Collection<Enterprise> enterprises) {
        return driverRepository.findByEnterprises(enterprises);
    }

    public void save(Driver driver) {
        driverRepository.save(driver);
    }

    public void deleteById(Long id) {
        driverRepository.deleteById(id);
    }
}
