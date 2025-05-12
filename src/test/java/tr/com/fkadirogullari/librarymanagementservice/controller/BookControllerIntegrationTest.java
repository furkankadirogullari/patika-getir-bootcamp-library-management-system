package tr.com.fkadirogullari.librarymanagementservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tr.com.fkadirogullari.librarymanagementservice.dto.BookRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.Role;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private BookRequest sampleBookRequest;

    @BeforeEach
    void setUp() {
        sampleBookRequest = BookRequest.builder()
                .title("Test Book")
                .author("Test Author")
                .isbn("1234567890")
                .genre("Fantasy")
                .publicationDate(LocalDate.of(2024,7,6))
                .quantity(26)
                .build();
    }

    @Test
    void shouldAddBookSuccessfully() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBookRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.isbn").value("1234567890"));
    }

    @Test
    void shouldGetBookByIsbn() throws Exception {
        // Önce kitap eklenir
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBookRequest)))
                .andExpect(status().isOk());

        // Sonra sorgulanır
        mockMvc.perform(get("/api/books/1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("Test Author"));
    }

    @Test
    void shouldUpdateBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBookRequest)))
                .andExpect(status().isOk());

        sampleBookRequest.setTitle("Updated Title");

        mockMvc.perform(put("/api/books/1234567890")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBookRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"));
    }

    @Test
    void shouldDeleteBook() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleBookRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/books/1234567890"))
                .andExpect(status().isOk());
    }

    // Diğer GET istekleri için benzer testler eklenebilir (title, author, genre)
}
