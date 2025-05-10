package tr.com.fkadirogullari.librarymanagementservice.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import tr.com.fkadirogullari.librarymanagementservice.config.JwtTokenProvider;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserLoginRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.Role;
import tr.com.fkadirogullari.librarymanagementservice.model.User;
import tr.com.fkadirogullari.librarymanagementservice.repository.UserRepository;

import java.security.Principal;
import java.util.Optional;
import java.util.Set;

public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private Principal principal;
    @Mock
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;
    @Mock
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_shouldSucceed() {
        UserRequest req = new UserRequest();
        req.setUserName("john");
        req.setEmail("john@example.com");
        req.setPassword("123456");

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(false);
        when(userRepository.existsByUserName(req.getUserName())).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("encoded-password");

        User saved = User.builder()
                .id(1L)
                .userName("john")
                .email("john@example.com")
                .password("encoded-password")
                .roles(Set.of(Role.ROLE_PATRON))
                .build();

        when(userRepository.save(any())).thenReturn(saved);

        var response = userService.register(req);

        assertNotNull(response);
        assertEquals("john@example.com", response.getEmail());
        verify(userRepository).save(any());
    }

    @Test
    void register_shouldFail_whenEmailExists() {
        UserRequest req = new UserRequest();
        req.setEmail("existing@example.com");

        when(userRepository.existsByEmail(req.getEmail())).thenReturn(true);

        Exception ex = assertThrows(IllegalArgumentException.class, () -> userService.register(req));
        assertEquals("Email already in use", ex.getMessage());
    }

    @Test
    void login_shouldSucceed() {
        UserLoginRequest req = new UserLoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("123456");

        User user = User.builder().email("user@example.com").build();

        when(userRepository.findByEmail(req.getEmail())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(user.getEmail(),user.getRoles())).thenReturn("mock-jwt");

        var token = userService.login(req);

        assertEquals("mock-jwt", token);
        verify(authenticationManager).authenticate(any());
    }

    @Test
    void getCurrentUser_shouldReturnUser() {

        User user = User.builder().email("user@example.com").userName("john").roles(Set.of(Role.ROLE_LIBRARIAN)).build();

        when(httpServletRequest.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        var response = userService.getCurrentUser();

        assertEquals("john", response.getUserName());
        assertTrue(response.getRoles().contains("ROLE_USER"));
    }
}
