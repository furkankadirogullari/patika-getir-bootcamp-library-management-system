package tr.com.fkadirogullari.librarymanagementservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.BorrowRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.response.BorrowResponse;
import tr.com.fkadirogullari.librarymanagementservice.service.contract.BorrowService;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    // Endpoint to allow a user with 'PATRON' role to borrow a book
    @Operation(
            summary = "User(patron) borrows books",
            description = "The user must have PATRON permission to borrow the book."
    )
    @PreAuthorize("hasRole('PATRON')") // Ensure only 'PATRON' role can access this endpoint
    @PostMapping("/borrow")
    public ResponseEntity<BorrowResponse> borrowBook(@RequestBody BorrowRequest request,
                                                     Authentication authentication) {
        String email = authentication.getName();
        BorrowResponse response = borrowService.borrowBook(email, request.getBookId());
        return ResponseEntity.ok(response);
    }

    // Endpoint to allow a user with 'PATRON' role to return a borrowed book
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


    // Endpoint to allow a user with 'PATRON' role to view their borrow history
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


    // Endpoint to allow a user with 'LIBRARIAN' role to view all borrow history
    @Operation(
            summary = "All borrow history is displayed",
            description = "This API requires LIBRARIAN authorization."
    )
    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/history/all")
    public ResponseEntity<List<BorrowResponse>> getAllBorrowHistory() {
        return ResponseEntity.ok(borrowService.getAllBorrowHistory());
    }

    // Endpoint to allow a user with 'LIBRARIAN' role to view overdue borrows
    @Operation(
            summary = "Displays overdue books",
            description = "This API requires LIBRARIAN authorization."
    )
    @PreAuthorize("hasRole('LIBRARIAN')")
    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowResponse>> getOverdueBorrows() {
        return ResponseEntity.ok(borrowService.getOverdueBorrows());
    }

    // Endpoint to export a report of overdue books as a CSV file (only for LIBRARIAN)
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

        // Write each overdue borrow entry to the CSV file
        for (BorrowResponse borrow : overdueBorrows) {
            writer.printf("%d,%s,%s,%s,%s%n",
                    borrow.getBorrowId(),
                    borrow.getBookTitle(),
                    borrow.getUserEmail(),
                    borrow.getBorrowDate(),
                    borrow.getDueDate());
        }

        writer.flush(); // Ensure all data is written to the response
        writer.close(); // Close the writer
    }


}
