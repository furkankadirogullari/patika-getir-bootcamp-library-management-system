package tr.com.fkadirogullari.librarymanagementservice.service;

import tr.com.fkadirogullari.librarymanagementservice.dto.UserLoginRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserResponse;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserUpdateRequest;

public interface UserService {

    UserResponse register(UserRequest request);
    String login(UserLoginRequest request);
    UserResponse getCurrentUser();
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
}
