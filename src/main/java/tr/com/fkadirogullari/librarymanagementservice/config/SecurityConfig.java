package tr.com.fkadirogullari.librarymanagementservice.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/login", "/api/users/register", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        // Librarian yetkisi gerektiren uçlar:
                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/api/users/{id}").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/api/borrows/history/all").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.GET, "/api/borrows/overdue/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("LIBRARIAN")

                        // Patron yetkisi gerektiren uçlar:
                        //.requestMatchers("/api/borrows/**").hasRole("PATRON")
                        .requestMatchers(HttpMethod.POST, "/api/borrows/**").hasRole("PATRON")
                        .requestMatchers(HttpMethod.GET, "/api/borrows/history").hasRole("PATRON")

                        // Ortak (giriş yapılmış herkese açık)
                        .requestMatchers(HttpMethod.GET, "/api/books/**").authenticated()
                        .requestMatchers("/api/users/me").authenticated()
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
