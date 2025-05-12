package tr.com.fkadirogullari.librarymanagementservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserLoginRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.Role;

import java.util.List;
import java.util.Set;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserControllerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    private WebTestClient webTestClient;

    private static String JWT_TOKEN;

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @Test
    @Order(1)
    void should_register_user() {
        UserRequest request = new UserRequest();
        request.setEmail("testuser@integration.com");
        request.setPassword("123456");
        request.setUserName("Integration Test");
        request.setRoles(Set.of(Role.ROLE_PATRON));

        webTestClient.post()
                .uri("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.email").isEqualTo("testuser@integration.com");
    }

    @Test
    @Order(2)
    void should_login_and_return_jwt_token() {
        UserLoginRequest loginRequest = new UserLoginRequest();
        loginRequest.setEmail("testuser@integration.com");
        loginRequest.setPassword("123456");

        webTestClient.post()
                .uri("/api/users/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(loginRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .consumeWith(response -> {
                    JWT_TOKEN = response.getResponseBody();
                    Assertions.assertNotNull(JWT_TOKEN);
                });
    }


}
