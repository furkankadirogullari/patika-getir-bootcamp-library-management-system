package tr.com.fkadirogullari.librarymanagementservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tr.com.fkadirogullari.librarymanagementservice.config.JwtTokenProvider;
import tr.com.fkadirogullari.librarymanagementservice.dto.BookRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.User;
import tr.com.fkadirogullari.librarymanagementservice.repository.UserRepository;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
public class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .userName("librarian")
                .email("librarian@example.com")
                .password(passwordEncoder.encode("123456"))
                .roles(Set.of("ROLE_USER"))
                .build();

        userRepository.save(user);
        jwtToken = jwtTokenProvider.generateToken(user.getEmail());
    }

    @Test
    void addBook_shouldSucceed() throws Exception {
        BookRequest request = new BookRequest();
        request.setTitle("Integration Book");
        request.setAuthor("Tester");
        request.setIsbn("1111111111111");
        request.setGenre("Science");

        mockMvc.perform(MockMvcRequestBuilders.post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + jwtToken)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.isbn").value("1111111111111"));
    }

    @Test
    void getAllBooks_shouldReturnBooks() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/api/books")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(0))));
    }
}
