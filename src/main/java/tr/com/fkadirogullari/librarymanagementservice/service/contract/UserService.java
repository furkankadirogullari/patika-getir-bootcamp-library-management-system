package tr.com.fkadirogullari.librarymanagementservice.service.contract;

import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.UserLoginRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.UserRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.response.UserResponse;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.UserUpdateRequest;

public interface UserService {

    UserResponse register(UserRequest request);
    String login(UserLoginRequest request);
    UserResponse getCurrentUser();
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserUpdateRequest request);
    void deleteUser(Long id);
}
