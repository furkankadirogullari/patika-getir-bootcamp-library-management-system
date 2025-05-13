package tr.com.fkadirogullari.librarymanagementservice.controller;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.UserLoginRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.UserRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.UserUpdateRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.Role;

import java.util.Set;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private static String JWT_TOKEN;

    @Test
    @Order(1)
    void should_register_user() {
        UserRequest request = new UserRequest("Integration Test", "testuser@integration.com", "123456", Set.of(Role.ROLE_PATRON));

        webTestClient.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @Order(2)
    void should_login_user_and_return_token() {
        UserLoginRequest loginRequest = new UserLoginRequest("testuser@integration.com", "123456");

        JWT_TOKEN = webTestClient.post()
                .uri("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .returnResult()
                .getResponseBody();
    }


    @Test
    @Order(4)
    void should_get_user_by_id_with_librarian_token() {
        // Librarian kullanıcı oluşturulup login edilir
        UserRequest librarian = new UserRequest("Librarian", "librarian@test.com", "123456", Set.of(Role.ROLE_LIBRARIAN));
        webTestClient.post().uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(librarian)
                .exchange().expectStatus().isOk();

        UserLoginRequest loginRequest = new UserLoginRequest("librarian@test.com", "123456");
        String librarianToken = webTestClient.post().uri("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange().expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        webTestClient.get().uri("/api/users/1") // 1 numaralı ID ilk kullanıcı
                .header("Authorization", "Bearer " + librarianToken)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.userName").isEqualTo("Integration Test");
    }

    @Test
    @Order(5)
    void should_update_user_with_librarian_token() {
        UserUpdateRequest update = new UserUpdateRequest("Updated User", "updated@integration.com", null);

        UserRequest librarian = new UserRequest("Librarian23", "librarian23@test.com", "123456", Set.of(Role.ROLE_LIBRARIAN));
        webTestClient.post().uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(librarian)
                .exchange().expectStatus().isOk();

        UserLoginRequest loginRequest = new UserLoginRequest("librarian23@test.com", "123456");
        String librarianToken = webTestClient.post().uri("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange().expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();


        webTestClient.put().uri("/api/users/1")
                .header("Authorization", "Bearer " + librarianToken) // token yeterliyse kullanılabilir
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(update)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo("updated@integration.com");
    }

    @Test
    @Order(6)
    void should_delete_user_with_librarian_token() {

        UserRequest librarian = new UserRequest("Librarian24", "librarian24@test.com", "123456", Set.of(Role.ROLE_LIBRARIAN));
        webTestClient.post().uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(librarian)
                .exchange().expectStatus().isOk();

        UserLoginRequest loginRequest = new UserLoginRequest("librarian24@test.com", "123456");
        String librarianToken = webTestClient.post().uri("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange().expectStatus().isOk()
                .expectBody(String.class)
                .returnResult().getResponseBody();

        webTestClient.delete().uri("/api/users/1")
                .header("Authorization", "Bearer " + librarianToken)
                .exchange()
                .expectStatus().isNoContent();
    }


}
