package ru.lessons.my.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.lessons.my.dto.EnterpriseDto;
import ru.lessons.my.model.Enterprise;
import ru.lessons.my.model.Manager;
import ru.lessons.my.repository.EnterpriseRepository;
import ru.lessons.my.repository.ManagerRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EnterpriseService {

    private final EnterpriseRepository enterpriseRepository;
    private final ManagerService managerService;

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

    @Transactional
    public void saveWithManager(Enterprise enterprise, Manager manager) {
        //Повторно ищем менеджера, чтобы он был в attached-статусе.
        //todo Улучшить дизайн
        Manager curManager = managerService.getManagerByUsername(manager.getUsername());
        enterprise.setManagers(Set.of(curManager));
        //Менеджер является владельцем связи, поэтому hibernate отслеживает его изменения.
        // Без этой строки не добавится связь между менеджером и предприятием.
        curManager.getEnterprises().add(enterprise);

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
