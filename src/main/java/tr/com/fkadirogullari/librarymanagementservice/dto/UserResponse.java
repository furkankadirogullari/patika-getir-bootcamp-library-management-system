package tr.com.fkadirogullari.librarymanagementservice.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@Builder
public class UserResponse {

    private Long id;
    private String userName;
    private String email;
    private Set<String> roles;
}
