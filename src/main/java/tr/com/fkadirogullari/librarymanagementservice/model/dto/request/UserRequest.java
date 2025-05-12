package tr.com.fkadirogullari.librarymanagementservice.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.Role;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {


    @NotBlank(message = "Username is required")
    private String userName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is invalid")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    private Set<Role> roles;
}
