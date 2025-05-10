package tr.com.fkadirogullari.librarymanagementservice.dto;

import lombok.Data;

@Data
public class UserUpdateRequest {

    private String userName;
    private String email;
    private String password;
}
