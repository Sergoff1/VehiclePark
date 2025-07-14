package ru.lessons.my.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lessons.my.model.Driver;
import ru.lessons.my.repository.DriverRepository;

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

    public void save(Driver driver) {
        driverRepository.save(driver);
    }

    public void deleteById(Long id) {
        driverRepository.deleteById(id);
    }
}
