package tr.com.fkadirogullari.librarymanagementservice.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.BorrowRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.response.BookResponse;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BorrowControllerIntegrationTest {

        @Autowired
        private WebTestClient webTestClient;

        private static String patronToken;
        private static String librarianToken;
        private static Long bookId;
        private static Long borrowId;

        @BeforeAll
        static void setup(@Autowired WebTestClient client) {
            // Register PATRON
            client.post()
                    .uri("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                    {
                      "userName": "FKPAT",
                      "email": "fkpp@borrow.com",
                      "password": "123456",
                      "roles": ["ROLE_PATRON"]
                    }
                """)
                    .exchange()
                    .expectStatus().isOk();

            // Register LIBRARIAN
            client.post()
                    .uri("/api/users/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                    {
                      "userName": "FKLIB",
                      "email": "fkll@borrow.com",
                      "password": "123456",
                      "roles": ["ROLE_LIBRARIAN"]
                    }
                """)
                    .exchange()
                    .expectStatus().isOk();

            // Patron login
            patronToken = client.post()
                    .uri("/api/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                    {
                      "email": "fkpp@borrow.com",
                      "password": "123456"
                    }
                """)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class)
                    .returnResult()
                    .getResponseBody();

            // Librarian login
            librarianToken = client.post()
                    .uri("/api/users/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("""
                    {
                      "email": "fkll@borrow.com",
                      "password": "123456"
                    }
                """)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(String.class)
                    .returnResult()
                    .getResponseBody();

            // Create book with librarian
            String bookPayload = """
            {
              "title": "Borrowable Book",
              "author": "Author",
              "isbn": "1234567890000",
              "publicationDate": "2020-01-01",
              "genre": "Test",
              "quantity": 3
            }
        """;

            BookResponse book = client.post()
                    .uri("/api/books")
                    .header("Authorization", "Bearer " + librarianToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(bookPayload)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(BookResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertNotNull(book);
            bookId = book.getId();


            BorrowRequest borrowRequest = new BorrowRequest();
            borrowRequest.setBookId(bookId);

        }

        @Test
        @Order(1)
        void should_borrow_book_with_patron_token() {
            BorrowRequest borrowRequest = new BorrowRequest();
            borrowRequest.setBookId(bookId);

            webTestClient.post()
                    .uri("/api/borrows/borrow")
                    .header("Authorization", "Bearer " + patronToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(borrowRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.bookTitle").isEqualTo("Borrowable Book")
                    .jsonPath("$.userEmail").isEqualTo("fkpp@borrow.com");
        }

        @Test
        @Order(2)
        void should_return_book_with_patron_token() {


            webTestClient.post()
                    .uri("/api/borrows/return/1")
                    .header("Authorization", "Bearer " + patronToken)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.returned").isEqualTo(true);

        }

        @Test
        @Order(3)
        void should_get_borrow_history_for_user() {
            webTestClient.get()
                    .uri("/api/borrows/history")
                    .header("Authorization", "Bearer " + patronToken)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$").isArray();
        }

        @Test
        @Order(4)
        void should_get_all_borrow_history_with_librarian_token() {
            webTestClient.get()
                    .uri("/api/borrows/history/all")
                    .header("Authorization", "Bearer " + librarianToken)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$").isArray();
        }

        @Test
        @Order(5)
        void should_get_overdue_borrow_list_with_librarian_token() {
            webTestClient.get()
                    .uri("/api/borrows/overdue")
                    .header("Authorization", "Bearer " + librarianToken)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$").isArray();
        }
    }

