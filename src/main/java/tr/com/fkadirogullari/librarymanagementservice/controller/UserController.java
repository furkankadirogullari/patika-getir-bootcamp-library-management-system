package tr.com.fkadirogullari.librarymanagementservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.UserLoginRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.UserRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.response.UserResponse;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.UserUpdateRequest;
import tr.com.fkadirogullari.librarymanagementservice.service.contract.UserService;

@Tag(name = "User Management", description = "Kullanıcı kayıt, giriş ve yönetimi")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Registers a new user (public endpoint)
    @Operation(
            summary = "User registration",
            description = "Creates a new user."
    )
    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody UserRequest request) {

        return userService.register(request);
    }

    // Authenticates the user and returns a JWT token (public endpoint)
    @Operation(
            summary = "User login",
            description = "User login is made and token is received"
    )
    @PostMapping("/login")
    public String login(@Valid @RequestBody UserLoginRequest request) {
        return userService.login(request);
    }

    // Retrieves user details by ID (only accessible by users with LIBRARIAN role)
    @Operation(
            summary = "Search for user by id",
            description = "Users are listed according to their user ID. You must have the LIBRARIAN role."
    )
    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // Updates user information by ID (only accessible by LIBRARIAN)
    @Operation(
            summary = "Update user information by id",
            description = "User information is updated according to the sent Id. To do this, you must have the LIBRARIAN role."
    )
    @PreAuthorize("hasRole('LIBRARIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateRequest req) {
        return ResponseEntity.ok(userService.updateUser(id, req));
    }

    // Deletes a user by ID (only accessible by LIBRARIAN)
    @Operation(
            summary = "Delete user information by id",
            description = "User information is deleted according to the sent Id. To do this, you must have the LIBRARIAN role."
    )
    @PreAuthorize("hasRole('LIBRARIAN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // Retrieves the information of the currently authenticated user
    @Operation(
            summary = "Returns user information",
            description = "Returns the user's own information"
    )
    @GetMapping("/me")
    public UserResponse getCurrentUser() {

        return userService.getCurrentUser();
    }
}
