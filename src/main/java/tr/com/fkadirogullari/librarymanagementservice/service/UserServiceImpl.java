package tr.com.fkadirogullari.librarymanagementservice.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tr.com.fkadirogullari.librarymanagementservice.config.JwtTokenProvider;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserLoginRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserResponse;
import tr.com.fkadirogullari.librarymanagementservice.exception.ResourceNotFoundException;
import tr.com.fkadirogullari.librarymanagementservice.model.User;
import tr.com.fkadirogullari.librarymanagementservice.repository.UserRepository;

import java.security.Principal;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final HttpServletRequest request;

    @Override
    public UserResponse register(UserRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (userRepository.existsByUserName(req.getUserName())) {
            throw new IllegalArgumentException("Username already in use");
        }

        User user = User.builder()
                .userName(req.getUserName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(Set.of("ROLE_USER"))
                .build();

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }

    @Override
    public String login(UserLoginRequest req) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));

        return jwtTokenProvider.generateToken(user.getEmail());
    }

    @Override
    public UserResponse getCurrentUser() {
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            throw new ResourceNotFoundException("No authenticated user");
        }

        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToResponse(user);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
    }
}
