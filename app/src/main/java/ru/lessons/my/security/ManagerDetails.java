package ru.lessons.my.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.time.ZoneId;
import java.util.Collection;

@Getter
public class ManagerDetails extends User {

    private final ZoneId timeZone;

    public ManagerDetails(String username, String password, Collection<? extends GrantedAuthority> authorities,
                          ZoneId timeZone) {
        super(username, password, authorities);
        this.timeZone = timeZone;
    }
}
