package tr.com.fkadirogullari.librarymanagementservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserLoginRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserResponse;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserUpdateRequest;
import tr.com.fkadirogullari.librarymanagementservice.service.UserService;

@Tag(name = "User Management", description = "Kullanıcı kayıt, giriş ve yönetimi")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;


    @Operation(
            summary = "User registration",
            description = "Creates a new user."
    )
    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody UserRequest request) {
        return userService.register(request);
    }

    @Operation(
            summary = "User login",
            description = "User login is made and token is received"
    )
    @PostMapping("/login")
    public String login(@Valid @RequestBody UserLoginRequest request) {
        return userService.login(request);
    }

    @Operation(
            summary = "Search for user by id",
            description = "Users are listed according to their user ID. You must have the LIBRARIAN role."
    )
    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }


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


    @Operation(
            summary = "Returns user information",
            description = "Returns the user's own information"
    )
    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        return userService.getCurrentUser();
    }
}
