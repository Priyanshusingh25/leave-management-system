package com.leaveflow.backend.security.user;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.leaveflow.backend.entity.Employee;

import java.util.Collection;
import java.util.List;
 
@Getter
public class UserPrincipal implements UserDetails {
 
    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String role;
    private final String name;
 
    public UserPrincipal(Employee employee) {
        this.id = employee.getId();
        this.email = employee.getEmail();
        this.passwordHash = employee.getPasswordHash();
        this.role = employee.getRole().name();
        this.name = employee.getName();
    }
 
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }
 
    @Override
    public String getPassword() {
        return passwordHash;
    }
 
    @Override
    public String getUsername() {
        return email;
    }
 
    @Override
    public boolean isAccountNonExpired() { return true; }
 
    @Override
    public boolean isAccountNonLocked() { return true; }
 
    @Override
    public boolean isCredentialsNonExpired() { return true; }
 
    @Override
    public boolean isEnabled() { return true; }
}
