package tr.com.fkadirogullari.librarymanagementservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tr.com.fkadirogullari.librarymanagementservice.config.JwtTokenProvider;
import tr.com.fkadirogullari.librarymanagementservice.dto.BorrowRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.Book;
import tr.com.fkadirogullari.librarymanagementservice.model.Role;
import tr.com.fkadirogullari.librarymanagementservice.model.User;
import tr.com.fkadirogullari.librarymanagementservice.repository.BookRepository;
import tr.com.fkadirogullari.librarymanagementservice.repository.UserRepository;

import java.util.Set;

@SpringBootTest
@AutoConfigureMockMvc
public class LoanControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private String jwtToken;
    private Book savedBook;

    @BeforeEach
    void setUp() throws Exception {
        // Kullanıcıyı kaydet (örnek)
        User user = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("123456"))
                .roles(Set.of(Role.ROLE_PATRON))
                .build();
        userRepository.save(user);

        jwtToken = jwtTokenProvider.generateToken(user.getEmail(),user.getRoles());

        // Kitap oluştur
        Book book = Book.builder()
                .title("Integration Test Book")
                .author("Test Author")
                .isbn("1234567890")
                .quantity(3)
                .build();
        savedBook = bookRepository.save(book);
    }

    @Test
    void borrowBook_shouldSucceed() throws Exception {
        BorrowRequest request = new BorrowRequest();
        request.setBookId(savedBook.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/borrows/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookTitle").value("Integration Test Book"))
                .andExpect(jsonPath("$.returned").value(false));
    }

    @Test
    void returnBook_shouldSucceed() throws Exception {
        // Önce kitap ödünç alalım
        BorrowRequest request = new BorrowRequest();
        request.setBookId(savedBook.getId());

        String response = mockMvc.perform(MockMvcRequestBuilders.post("/api/borrows/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long borrowId = objectMapper.readTree(response).get("id").asLong();

        // Şimdi kitabı iade edelim
        mockMvc.perform(MockMvcRequestBuilders.post("/api/borrows/return/" + borrowId)
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.returned").value(true))
                .andExpect(jsonPath("$.returnDate").exists());
    }

    @Test
    void borrowBook_shouldFail_whenQuantityZero() throws Exception {
        // Kitap stoğunu sıfırla
        savedBook.setQuantity(0);
        bookRepository.save(savedBook);

        BorrowRequest request = new BorrowRequest();
        request.setBookId(savedBook.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/borrows/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void getMyLoans_shouldReturnLoansForCurrentUser() throws Exception {
        // Ödünç al
        BorrowRequest request = new BorrowRequest();
        request.setBookId(savedBook.getId());

        mockMvc.perform(MockMvcRequestBuilders.post("/api/borrows/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());

        // Listele
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/borrows")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

}
