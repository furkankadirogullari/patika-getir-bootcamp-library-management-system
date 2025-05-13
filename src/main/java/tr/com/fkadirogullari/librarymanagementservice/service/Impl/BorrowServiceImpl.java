package tr.com.fkadirogullari.librarymanagementservice.service.Impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.response.BorrowResponse;
import tr.com.fkadirogullari.librarymanagementservice.exception.ResourceNotFoundException;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.Book;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.Borrow;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.User;
import tr.com.fkadirogullari.librarymanagementservice.publisher.ReactiveBookAvailability;
import tr.com.fkadirogullari.librarymanagementservice.repository.BookRepository;
import tr.com.fkadirogullari.librarymanagementservice.repository.BorrowRepository;
import tr.com.fkadirogullari.librarymanagementservice.repository.UserRepository;
import tr.com.fkadirogullari.librarymanagementservice.service.contract.BorrowService;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService {

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final HttpServletRequest request;
    private final ReactiveBookAvailability reactiveBookAvailability;

    // Allows a user to borrow a book if it's available and they have no active borrows
    @Override
    public BorrowResponse borrowBook(String email, Long bookId) {

        // Retrieve user by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Retrieve book by ID
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        // Check if the book is already borrowed and not returned
        if (borrowRepository.existsByBookAndReturnedFalse(book)) {
            throw new IllegalStateException("Book is currently not available");
        }

        // Check if the user already has an active borrow
        if (borrowRepository.existsByUserAndReturnedFalse(user)) {
            throw new IllegalStateException("User already has an active borrow");
        }

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14); // Borrowing period: 14 days

        // Create and set up the borrow record
        Borrow borrow = new Borrow();
        borrow.setBook(book);
        borrow.setUser(user);
        borrow.setBorrowDate(borrowDate);
        borrow.setDueDate(dueDate);
        borrow.setReturned(false);

        // Check if the book is in stock
        if (book.getQuantity() <= 0) {
            throw new IllegalStateException("Book is out of stock");
        }

        // Decrease the book's quantity since it's being borrowed
        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);

        // Notify subscribers that book availability has changed (Reactive)
        reactiveBookAvailability.publish(book);

        // Save borrow record to DB
        Borrow saved = borrowRepository.save(borrow);

        // Return borrow details
        return new BorrowResponse(
                saved.getId(),
                book.getId(),
                book.getTitle(),
                user.getEmail(),
                borrowDate,
                dueDate,
                borrow.isReturned()
        );
    }

    // Allows a user to return a borrowed book
    @Override
    public BorrowResponse returnBook(String email, Long borrowId) {

        // Retrieve user and borrow records
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        // Ensure the borrow record belongs to the requesting user
        if (!borrow.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only return your own borrow");
        }

        // Check if already returned
        if (borrow.isReturned()) {
            throw new IllegalStateException("This borrow has already been returned");
        }

        // Mark borrow as returned and update return date
        borrow.setReturned(true);
        borrow.setReturnDate(LocalDate.now());

        // Increase the quantity of the book
        Book book = borrow.getBook();
        book.setQuantity(book.getQuantity() + 1);
        bookRepository.save(book);


        // Publish updated book status (Reactive)
        reactiveBookAvailability.publish(book);

        // Save updated borrow record
        Borrow updated = borrowRepository.save(borrow);

        // Return updated borrow info
        return new BorrowResponse(
                updated.getId(),
                updated.getBook().getId(),
                updated.getBook().getTitle(),
                updated.getUser().getEmail(),
                updated.getBorrowDate(),
                updated.getDueDate(),
                updated.isReturned()
        );
    }

    // Returns borrowing history for a specific user
    @Override
    public List<BorrowResponse> getUserBorrowHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Borrow> borrows = borrowRepository.findAllByUser(user);

        return borrows.stream()
                .map(borrow -> new BorrowResponse(
                        borrow.getId(),
                        borrow.getBook().getId(),
                        borrow.getBook().getTitle(),
                        user.getEmail(),
                        borrow.getBorrowDate(),
                        borrow.getDueDate(),
                        borrow.isReturned()
                ))
                .toList();
    }

    // Returns the borrowing history of all users (accessible by librarians)
    @Override
    public List<BorrowResponse> getAllBorrowHistory() {
        return borrowRepository.findAll().stream()
                .map(borrow -> new BorrowResponse(
                        borrow.getId(),
                        borrow.getBook().getId(),
                        borrow.getBook().getTitle(),
                        borrow.getUser().getEmail(),
                        borrow.getBorrowDate(),
                        borrow.getDueDate(),
                        borrow.isReturned()
                ))
                .toList();
    }

    // Returns a list of overdue borrows
    @Override
    public List<BorrowResponse> getOverdueBorrows() {
        List<Borrow> overdueBorrows = borrowRepository.findByReturnedFalseAndDueDateBefore(LocalDate.now());

        return overdueBorrows.stream()
                .map(borrow -> new BorrowResponse(
                        borrow.getId(),
                        borrow.getBook().getId(),
                        borrow.getBook().getTitle(),
                        borrow.getUser().getEmail(),
                        borrow.getBorrowDate(),
                        borrow.getDueDate(),
                        borrow.isReturned()
                ))
                .toList();
    }


}
