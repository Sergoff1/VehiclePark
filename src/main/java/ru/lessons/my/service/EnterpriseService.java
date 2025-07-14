package ru.lessons.my.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lessons.my.model.Enterprise;
import ru.lessons.my.repository.EnterpriseRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnterpriseService {

    private final EnterpriseRepository enterpriseRepository;

    public List<Enterprise> findAll() {
        return enterpriseRepository.findAll();
    }

    public Enterprise findById(Long id) {
        return enterpriseRepository.findById(id);
    }

    public void save(Enterprise enterprise) {
        enterpriseRepository.save(enterprise);
    }

    public void deleteById(Long id) {
        enterpriseRepository.deleteById(id);
    }
}
