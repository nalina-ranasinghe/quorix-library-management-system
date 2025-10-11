// New CustomUserDetailsService.java (inferred from references, added PENDING check)
package com.library.app.service;

import com.library.app.entity.User;
import com.library.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        if ("PENDING".equals(user.getStatus())) {
            throw new UsernameNotFoundException("User pending approval");
        }
        Set<String> roles = userRepository.findRolesByUserId(user.getUserId());
        user.setRoles(roles);
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles(roles.toArray(new String[0]))
                .build();
    }
}