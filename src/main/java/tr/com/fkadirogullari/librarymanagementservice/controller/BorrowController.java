package tr.com.fkadirogullari.librarymanagementservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.com.fkadirogullari.librarymanagementservice.dto.BorrowRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.BorrowResponse;
import tr.com.fkadirogullari.librarymanagementservice.model.Borrow;
import tr.com.fkadirogullari.librarymanagementservice.service.BorrowService;

import java.util.List;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    // 📚 Kitap ödünç alma
    @PostMapping("/borrow")
    public ResponseEntity<BorrowResponse> borrowBook(@Valid @RequestBody BorrowRequest request) {
        BorrowResponse response = borrowService.borrowBook(request);
        return ResponseEntity.ok(response);
    }

    // 🔄 Kitap iade etme
    @PostMapping("/return/{loanId}")
    public ResponseEntity<BorrowResponse> returnBook(@PathVariable Long loanId) {
        BorrowResponse response = borrowService.returnBook(loanId);
        return ResponseEntity.ok(response);
    }

    // 📋 Kullanıcının ödünç kayıtlarını listeleme
    @GetMapping
    public ResponseEntity<List<BorrowResponse>> getMyLoans(@RequestParam(defaultValue = "false") boolean activeOnly) {
        List<BorrowResponse> loans = borrowService.getMyBorrows(activeOnly);
        return ResponseEntity.ok(loans);
    }
}
