package tr.com.fkadirogullari.librarymanagementservice.service;

import jakarta.servlet.http.HttpServletRequest;
//import org.h2.engine.Role;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import tr.com.fkadirogullari.librarymanagementservice.security.JwtTokenProvider;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.UserLoginRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.UserRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.response.UserResponse;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.UserUpdateRequest;
import tr.com.fkadirogullari.librarymanagementservice.exception.ResourceNotFoundException;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.User;
import tr.com.fkadirogullari.librarymanagementservice.repository.UserRepository;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.Role;
import tr.com.fkadirogullari.librarymanagementservice.service.Impl.UserServiceImpl;

import java.security.Principal;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testRegister() {
        UserRequest userRequest = new UserRequest("john_doe", "john@example.com", "password", Set.of(Role.ROLE_PATRON));
        User user = new User(1L, "john_doe", "john@example.com", "encoded_password", Set.of(Role.ROLE_PATRON));
        when(userRepository.existsByEmail(userRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByUserName(userRequest.getUserName())).thenReturn(false);
        when(passwordEncoder.encode(userRequest.getPassword())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserResponse userResponse = userService.register(userRequest);

        assertEquals("john_doe", userResponse.getUserName());
        assertEquals("john@example.com", userResponse.getEmail());
    }

    @Test
    void testLogin() {
        UserLoginRequest loginRequest = new UserLoginRequest("john@example.com", "password");
        User user = new User(1L, "john_doe", "john@example.com", "encoded_password", Set.of(Role.ROLE_PATRON));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken("john@example.com", Set.of(Role.ROLE_PATRON))).thenReturn("jwt_token");

        String token = userService.login(loginRequest);

        assertEquals("jwt_token", token);
    }


    @Test
    void testGetUserById_Success() {
        // Arrange
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .userName("john_doe")
                .email("john@example.com")
                .roles(Set.of(Role.ROLE_LIBRARIAN, Role.ROLE_PATRON))
                .build();

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.getUserById(userId);

        // Assert
        Assertions.assertEquals(userId, response.getId());
        Assertions.assertEquals("john_doe", response.getUserName());
        Assertions.assertEquals("john@example.com", response.getEmail());
        Assertions.assertTrue(response.getRoles().contains("ROLE_LIBRARIAN"));
        Assertions.assertTrue(response.getRoles().contains("ROLE_PATRON"));
    }

    @Test
    void testGetUserById_UserNotFound() {
        // Arrange
        Long userId = 99L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(userId);
        });
    }


    @Test
    void testUpdateUser_WithPassword() {
        // Arrange
        Long userId = 1L;
        User existingUser = User.builder()
                .id(userId)
                .userName("old_name")
                .email("old@example.com")
                .password("oldpass")
                .roles(Set.of(Role.ROLE_PATRON))
                .build();

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                "new_name",
                "new@example.com",
                "newpass"
        );

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        Mockito.when(passwordEncoder.encode("newpass")).thenReturn("encodedpass");
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserResponse response = userService.updateUser(userId, updateRequest);

        // Assert
        Assertions.assertEquals("new_name", response.getUserName());
        Assertions.assertEquals("new@example.com", response.getEmail());
        Assertions.assertTrue(response.getRoles().contains("ROLE_PATRON"));
        Assertions.assertEquals("encodedpass", existingUser.getPassword());
    }

    @Test
    void testUpdateUser_WithoutPassword() {
        // Arrange
        Long userId = 2L;
        User existingUser = User.builder()
                .id(userId)
                .userName("old_user")
                .email("old@email.com")
                .password("existing_hashed_pass")
                .roles(Set.of(Role.ROLE_PATRON))
                .build();

        UserUpdateRequest updateRequest = new UserUpdateRequest(
                "updated_user",
                "updated@email.com",
                "" // boş password
        );

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));
        Mockito.when(userRepository.save(Mockito.any(User.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        UserResponse response = userService.updateUser(userId, updateRequest);

        // Assert
        Assertions.assertEquals("updated_user", response.getUserName());
        Assertions.assertEquals("updated@email.com", response.getEmail());
        Assertions.assertEquals("existing_hashed_pass", existingUser.getPassword()); // şifre değişmedi
    }

    @Test
    void testUpdateUser_UserNotFound() {
        // Arrange
        Long userId = 3L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        UserUpdateRequest updateRequest = new UserUpdateRequest("name", "email", "pass");

        // Act & Assert
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            userService.updateUser(userId, updateRequest);
        });
    }


    @Test
    void testDeleteUser_Success() {
        // Arrange
        Long userId = 1L;
        User user = User.builder()
                .id(userId)
                .userName("testuser")
                .email("test@example.com")
                .password("password")
                .roles(Set.of(Role.ROLE_PATRON))
                .build();

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        Mockito.doNothing().when(userRepository).delete(user);

        // Act
        userService.deleteUser(userId);

        // Assert
        Mockito.verify(userRepository).delete(user);
    }

    @Test
    void testDeleteUser_UserNotFound() {
        // Arrange
        Long userId = 2L;
        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(userId);
        });
    }

    @Test
    void testGetCurrentUser_Success() {
        // Arrange
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(request.getUserPrincipal()).thenReturn(principal);
        Mockito.when(principal.getName()).thenReturn("john@example.com");

        User user = User.builder()
                .id(1L)
                .userName("john_doe")
                .email("john@example.com")
                .password("hashedpass")
                .roles(Set.of(Role.ROLE_PATRON))
                .build();

        Mockito.when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        // Act
        UserResponse response = userService.getCurrentUser();

        // Assert
        Assertions.assertEquals("john_doe", response.getUserName());
        Assertions.assertEquals("john@example.com", response.getEmail());
        Assertions.assertTrue(response.getRoles().contains("ROLE_PATRON"));
    }

    @Test
    void testGetCurrentUser_Unauthenticated() {
        // Arrange
        Mockito.when(request.getUserPrincipal()).thenReturn(null);

        // Act & Assert
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            userService.getCurrentUser();
        });
    }
}
