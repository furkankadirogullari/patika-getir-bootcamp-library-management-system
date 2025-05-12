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

    // Dependencies injected via constructor
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final HttpServletRequest request;


    // Registers a new user by checking for uniqueness, encoding password, and saving to DB
    @Override
    public UserResponse register(UserRequest req) {

        // Check if email is already registered
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Check if username is already taken
        if (userRepository.existsByUserName(req.getUserName())) {
            throw new IllegalArgumentException("Username already in use");
        }

        Set<Role> user_role = req.getRoles(); // Get user roles from the request

        // Build the user entity
        User user = User.builder()
                .userName(req.getUserName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))    // Encode the password
                .roles(user_role) // Assign roles (e.g. ROLE_PATRON, ROLE_LIBRARIAN)
                .build();

        // Save to DB and return the mapped response
        User saved = userRepository.save(user);
        return mapToResponse(saved);
    }


    // Authenticates the user and returns a JWT token
    @Override
    public String login(UserLoginRequest req) {

        // Perform authentication (throws exception if invalid)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );

        // Retrieve user by email
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid email or password"));


        // Generate JWT with user's email and roles
        return jwtTokenProvider.generateToken(user.getEmail(),user.getRoles());
    }


    // Fetches user details by user ID (used by librarian roles)
    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Convert Role enums to String names
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

    // Returns currently authenticated user's information
    @Override
    public UserResponse getCurrentUser() {

        // Retrieve security principal from request
        Principal principal = request.getUserPrincipal();
        if (principal == null) {
            throw new ResourceNotFoundException("No authenticated user");
        }

        // Fetch user based on email from principal
        String email = principal.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapToResponse(user);
    }


    // Updates a user's information, including password if provided
    @Override
    public UserResponse updateUser(Long id, UserUpdateRequest req) {

        // Fetch existing user by ID
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Update basic info
        user.setUserName(req.getUserName());
        user.setEmail(req.getEmail());

        // Update password only if it's not blank
        if (req.getPassword() != null && !req.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        // Save updated user
        userRepository.save(user);

        // Convert roles for response
        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        return new UserResponse(user.getId(), user.getUserName(), user.getEmail(), roles);
    }


    // Deletes a user from the system
    @Override
    public void deleteUser(Long id) {

        // Retrieve user or throw if not found
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        userRepository.delete(user);
    }

    // Utility method to convert User entity to UserResponse DTO
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
