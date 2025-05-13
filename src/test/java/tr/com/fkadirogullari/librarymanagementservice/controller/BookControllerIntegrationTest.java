package tr.com.fkadirogullari.librarymanagementservice.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BookControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private static String librarianToken;
    private static Long createdBookId;

    @BeforeAll
    static void setup(@Autowired WebTestClient client) {
        // 1. LIBRARIAN kullanıcı kaydı
        String registerPayload = """
            {
              "userName": "frknk96",
              "email": "librarianfrknk@books.com",
              "password": "password123",
              "roles": ["ROLE_LIBRARIAN"]
            }
        """;

        client.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(registerPayload)
                .exchange()
                .expectStatus().isOk();

        // 2. Giriş yap ve token al
        String loginPayload = """
            {
              "email": "librarianfrknk@books.com",
              "password": "password123"
            }
        """;

        var responseBody = client.post()
                .uri("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginPayload)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();

        librarianToken = responseBody;
    }

    @Test
    @Order(1)
    void should_add_book_with_librarian_token() {
        String bookPayload = """
            {
              "title": "The Lord of the Rings Two Towers",
              "author": "J.R.R Tolkienn",
              "isbn": "9999999999992",
              "publicationDate": "1937-01-02",
              "genre": "Fantasticc",
              "quantity": 10
            }
        """;

        createdBookId = webTestClient.post()
                .uri("/api/books/book")
                .header("Authorization", "Bearer " + librarianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(bookPayload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("The Lord of the Rings Two Towers")
                .jsonPath("$.isbn").isEqualTo("9999999999992")
                .jsonPath("$.quantity").isEqualTo(10)
                .jsonPath("$.id").exists()
                .returnResult()
                .getResponseBody()
                .hashCode() + 0L; // Dummy assignment for test continuity


    }

    @Test
    @Order(2)
    void should_get_book_by_isbn() {
        webTestClient.get()
                .uri("/api/books/9999999999992")
                .header("Authorization", "Bearer " + librarianToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("The Lord of the Rings Two Towers");
    }

    @Test
    @Order(3)
    void should_get_book_by_title() {
        webTestClient.get()
                .uri("/api/books/title/The Lord of the Rings Two Towers")
                .header("Authorization", "Bearer " + librarianToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].isbn").isEqualTo("9999999999992");
    }

    @Test
    @Order(4)
    void should_get_book_by_author() {
        webTestClient.get()
                .uri("/api/books/author/J.R.R Tolkienn")
                .header("Authorization", "Bearer " + librarianToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("The Lord of the Rings Two Towers");
    }

    @Test
    @Order(5)
    void should_get_book_by_genre() {
        webTestClient.get()
                .uri("/api/books/genre/Fantasticc")
                .header("Authorization", "Bearer " + librarianToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].title").isEqualTo("The Lord of the Rings Two Towers");
    }

    @Test
    @Order(6)
    void should_get_all_books() {
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/books")
                        .queryParam("keyword", "The Lord")
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .header("Authorization", "Bearer " + librarianToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content[0].title").isEqualTo("The Lord of the Rings Two Towers");
    }

    @Test
    @Order(7)
    void should_update_book() {
        String updatePayload = """
            {
              "title": "The Lord of the Rings Two Towers - Updated",
              "author": "J.R.R Tolkienn",
              "isbn": "9999999999992",
              "publicationDate": "1937-01-02",
              "genre": "Fantasticc",
              "quantity": 10
            }
        """;

        webTestClient.put()
                .uri("/api/books/9999999999992")
                .header("Authorization", "Bearer " + librarianToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updatePayload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.title").isEqualTo("The Lord of the Rings Two Towers - Updated")
                .jsonPath("$.quantity").isEqualTo(10);
    }

    @Test
    @Order(8)
    void should_delete_book() {
        webTestClient.delete()
                .uri("/api/books/9999999999992")
                .header("Authorization", "Bearer " + librarianToken)
                .exchange()
                .expectStatus().isNoContent();


    }

    

}
