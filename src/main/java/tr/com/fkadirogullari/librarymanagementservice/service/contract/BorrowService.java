package tr.com.fkadirogullari.librarymanagementservice.service.contract;

import tr.com.fkadirogullari.librarymanagementservice.model.dto.response.BorrowResponse;

import java.util.List;

public interface BorrowService {

    BorrowResponse borrowBook(String userEmail, Long bookId);
    BorrowResponse returnBook(String userEmail, Long loanId);
    List<BorrowResponse> getUserBorrowHistory(String email);
    List<BorrowResponse> getAllBorrowHistory();
    List<BorrowResponse> getOverdueBorrows();
}
