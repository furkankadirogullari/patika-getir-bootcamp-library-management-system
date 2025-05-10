package tr.com.fkadirogullari.librarymanagementservice.service;

import tr.com.fkadirogullari.librarymanagementservice.dto.BorrowRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.BorrowResponse;

import java.util.List;

public interface BorrowService {

    BorrowResponse borrowBook(BorrowRequest request);
    BorrowResponse returnBook(Long borrowId);
    List<BorrowResponse> getMyBorrows(boolean onlyActive);
}
