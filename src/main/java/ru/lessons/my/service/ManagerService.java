package ru.lessons.my.service;

import jakarta.persistence.NoResultException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.lessons.my.model.entity.Manager;
import ru.lessons.my.repository.ManagerRepository;
import ru.lessons.my.security.ManagerDetails;
import ru.lessons.my.util.TimeZoneContext;

import java.time.ZoneId;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ManagerService implements UserDetailsService {

    private final ManagerRepository managerRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Manager manager = getManagerByUsername(username);

        return new ManagerDetails(manager.getUsername(), manager.getPassword(),
                AuthorityUtils.createAuthorityList("API"), ZoneId.of(Objects.requireNonNullElse(TimeZoneContext.get(), "UTC")));
    }

    public Manager getManagerByUsername(String username) {
        try {
            return managerRepository.getByUsername(username);
        } catch (NoResultException e) {
            throw new UsernameNotFoundException(String.format("Manager %s not found", username));
        }

    }
}
