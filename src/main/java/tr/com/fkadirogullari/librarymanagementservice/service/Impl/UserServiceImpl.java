package tr.com.fkadirogullari.librarymanagementservice.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tr.com.fkadirogullari.librarymanagementservice.security.JwtTokenProvider;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.UserLoginRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.UserRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.response.UserResponse;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.UserUpdateRequest;
import tr.com.fkadirogullari.librarymanagementservice.exception.ResourceNotFoundException;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.Role;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.User;
import tr.com.fkadirogullari.librarymanagementservice.repository.UserRepository;
import tr.com.fkadirogullari.librarymanagementservice.service.contract.UserService;

import java.security.Principal;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final HttpServletRequest request;


    // Registers a new user by encoding the password and saving to DB
    @Override
    public UserResponse register(UserRequest req) {

        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        if (userRepository.existsByUserName(req.getUserName())) {
            throw new IllegalArgumentException("Username already in use");
        }

        Set<Role> user_role = req.getRoles();


        User user = User.builder()
                .userName(req.getUserName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .roles(user_role) // Assign roles (e.g. ROLE_PATRON, ROLE_LIBRARIAN)
                .build();

        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }


    // Authenticates the user and returns a JWT token
    @Override
    public String login(UserLoginRequest req) {

        // Verify credentials
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        // Load user by email
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));



        return jwtTokenProvider.generateToken(user.getEmail(),user.getRoles());
    }


    // Retrieves a user by ID (used by librarians)
    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        Set<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getUserName(),
                user.getEmail(),
                roleNames
        );
    }

    // Retrieves the details of the currently logged-in user
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


    // Updates user info (allowed for librarians)
    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        user.setUserName(req.getUserName());
        user.setEmail(req.getEmail());

        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        userRepository.save(user);

        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new UserResponse(user.getId(), user.getUserName(), user.getEmail(), roles);
    }


    // Deletes a user (only librarians can perform this)
    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(user);
    }

    private UserResponse mapToResponse(User user) {
        Set<String> roleNames = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        return UserResponse.builder()
                .id(user.getId())
                .userName(user.getUserName())
                .email(user.getEmail())
                .roles(roleNames)
                .build();
    }
}
