package ru.lessons.my.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lessons.my.dto.EnterpriseDto;
import ru.lessons.my.model.entity.Enterprise;
import ru.lessons.my.model.entity.Manager;
import ru.lessons.my.repository.EnterpriseRepository;
import ru.lessons.my.security.SecurityUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnterpriseService {

    private final EnterpriseRepository enterpriseRepository;
    private final SecurityUtils securityUtils;

    public List<Enterprise> findAll() {
        return enterpriseRepository.findAll();
    }

    public List<Enterprise> findByManager(Manager manager) {
        return enterpriseRepository.findByManager(manager);
    }

    public Enterprise findById(Long id) {
        return enterpriseRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Enterprise with id %s not found", id)));
    }

    public void save(Enterprise enterprise) {
        enterpriseRepository.save(enterprise);
    }

    public void saveWithCurrentManager(Enterprise enterprise) {
        enterprise.getManagers().add(securityUtils.getCurrentManager());

        enterpriseRepository.save(enterprise);
    }

    public void update(EnterpriseDto enterprise) {
        Enterprise oldEnterprise = enterpriseRepository.findById(enterprise.getId())
                .orElseThrow(() -> new EntityNotFoundException(
                        String.format("Enterprise with id %s not found", enterprise.getId())));
        oldEnterprise.setName(enterprise.getName());
        oldEnterprise.setCity(enterprise.getCity());
        enterpriseRepository.save(oldEnterprise);
    }

    public void deleteById(Long id) {
        enterpriseRepository.deleteById(id);
    }
}
