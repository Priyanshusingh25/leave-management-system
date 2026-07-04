package com.leaveflow.backend.security.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.leaveflow.backend.repository.EmployeeRepository;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

   private final EmployeeRepository employeeRepository;

   @Override
   public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
       return employeeRepository.findByEmail(email)
               .map(UserPrincipal::new)
               .orElseThrow(() -> new UsernameNotFoundException("No account found for email: " + email));
   }
}
