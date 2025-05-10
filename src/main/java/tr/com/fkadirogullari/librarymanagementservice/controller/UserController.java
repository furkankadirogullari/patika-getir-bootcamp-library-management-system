package tr.com.fkadirogullari.librarymanagementservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserLoginRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserResponse;
import tr.com.fkadirogullari.librarymanagementservice.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody UserRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public String login(@Valid @RequestBody UserLoginRequest request) {
        return userService.login(request);
    }

    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        return userService.getCurrentUser();
    }
}
