package tr.com.fkadirogullari.librarymanagementservice.model.dto.response;

import lombok.*;

import java.time.LocalDate;

@Data
@Getter
@Setter
@Builder

public class BorrowResponse {

    private Long borrowId;
    private Long bookId;
    private String bookTitle;
    private String userEmail;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private boolean returned;

    public BorrowResponse(Long borrowId, Long bookId, String bookTitle, String userEmail, LocalDate borrowDate, LocalDate dueDate,boolean returned) {
        this.borrowId = borrowId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.userEmail = userEmail;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returned = returned;
    }
}
