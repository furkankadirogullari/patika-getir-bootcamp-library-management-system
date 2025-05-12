package tr.com.fkadirogullari.librarymanagementservice.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BorrowRequest {

    @NotNull
    private Long bookId;
}
