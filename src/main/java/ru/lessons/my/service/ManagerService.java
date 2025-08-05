package ru.lessons.my.service;

import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.lessons.my.model.Manager;
import ru.lessons.my.repository.ManagerRepository;

@Service
@RequiredArgsConstructor
public class ManagerService implements UserDetailsService {

    private final ManagerRepository managerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Manager manager = getManagerByUsername(username);

        return User.builder()
                .username(manager.getUsername())
                .password(manager.getPassword())
                .authorities("API")
                .build();
    }

    public Manager getManagerByUsername(String username) {
        try {
            return managerRepository.getByUsername(username);
        } catch (NoResultException e) {
            throw new UsernameNotFoundException(String.format("Manager %s not found", username));
        }

    }
}
