package tr.com.fkadirogullari.librarymanagementservice.service.Impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.User;
import tr.com.fkadirogullari.librarymanagementservice.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor

// Custom implementation of UserDetailsService for Spring Security authentication
public class CustomUserDetailsService implements UserDetailsService{

        private final UserRepository userRepository;

        // Loads a user by email (used as username) and builds UserDetails object for Spring Security
        @Override
        public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

            // Fetch the user from the database by email
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

            // Return a Spring Security UserDetails object with email, password and granted authorities (roles)
            return new org.springframework.security.core.userdetails.User(
                    user.getEmail(),
                    user.getPassword(),
                    user.getRoles().stream()
                            .map(role -> new SimpleGrantedAuthority(role.name()))
                            .collect(Collectors.toSet())
            );
        }
    }
