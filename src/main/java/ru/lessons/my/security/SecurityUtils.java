package ru.lessons.my.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.lessons.my.model.entity.Manager;
import ru.lessons.my.service.ManagerService;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final ManagerService managerService;

    public Manager getCurrentManager() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return managerService.getManagerByUsername(auth.getName());
    }
}
