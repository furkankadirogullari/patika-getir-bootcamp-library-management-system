package tr.com.fkadirogullari.librarymanagementservice.controller;

import io.jsonwebtoken.Jwt;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import tr.com.fkadirogullari.librarymanagementservice.dto.BorrowRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.BorrowResponse;
import tr.com.fkadirogullari.librarymanagementservice.model.Borrow;
import tr.com.fkadirogullari.librarymanagementservice.service.BorrowService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;


    @Operation(
            summary = "User(patron) borrows books",
            description = "The user must have PATRON permission to borrow the book."
    )
    @PreAuthorize("hasRole('PATRON')")
    @PostMapping("/borrow")
    public ResponseEntity<BorrowResponse> borrowBook(@RequestBody BorrowRequest request,
                                                     Authentication authentication) {
        String email = authentication.getName();
        BorrowResponse response = borrowService.borrowBook(email, request.getBookId());
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "User(patron) return borrow",
            description = "The user must have PATRON permission to return the borrow."
    )
    @PreAuthorize("hasRole('PATRON')")
    @PostMapping("/return/{borrowId}")
    public ResponseEntity<BorrowResponse> returnBook(@PathVariable Long borrowId,
                                                   Authentication authentication) {
        String email = authentication.getName();
        BorrowResponse response = borrowService.returnBook(email, borrowId);
        return ResponseEntity.ok(response);
    }


    @Operation(
            summary = "View user borrow history",
            description = "This API requires PATRON authorization."
    )
    @PreAuthorize("hasRole('PATRON')")
    @GetMapping("/history")
    public ResponseEntity<List<BorrowResponse>> getMyBorrowHistory(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(borrowService.getUserBorrowHistory(email));
    }


    @Operation(
            summary = "All borrow history is displayed",
            description = "This API requires LIBRARIAN authorization."
    )
    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/history/all")
    public ResponseEntity<List<BorrowResponse>> getAllBorrowHistory() {
        return ResponseEntity.ok(borrowService.getAllBorrowHistory());
    }


    @Operation(
            summary = "Displays overdue books",
            description = "This API requires LIBRARIAN authorization."
    )
    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowResponse>> getOverdueBorrows() {
        return ResponseEntity.ok(borrowService.getOverdueBorrows());
    }


    @Operation(
            summary = "Returns the report of overdue books",
            description = "This API requires LIBRARIAN authorization."
    )
    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping(value = "/overdue/report", produces = "text/csv")
    public void exportOverdueBorrowsReport(HttpServletResponse response) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"overdue_borrows.csv\"");

        List<BorrowResponse> overdueBorrows = borrowService.getOverdueBorrows();

        PrintWriter writer = response.getWriter();
        writer.println("Borrow ID,Book Title,User Email,Borrow Date,Due Date");

        for (BorrowResponse borrow : overdueBorrows) {
            writer.printf("%d,%s,%s,%s,%s%n",
                    borrow.getBorrowId(),
                    borrow.getBookTitle(),
                    borrow.getUserEmail(),
                    borrow.getBorrowDate(),
                    borrow.getDueDate());
        }

        writer.flush();
        writer.close();
    }


}
