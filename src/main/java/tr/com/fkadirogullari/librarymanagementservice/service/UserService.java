package tr.com.fkadirogullari.librarymanagementservice.service;

import tr.com.fkadirogullari.librarymanagementservice.dto.UserLoginRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.UserResponse;

public interface UserService {

    UserResponse register(UserRequest request);
    String login(UserLoginRequest request);
    UserResponse getCurrentUser();
}
