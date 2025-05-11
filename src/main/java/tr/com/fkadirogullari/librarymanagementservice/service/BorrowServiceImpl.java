package tr.com.fkadirogullari.librarymanagementservice.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import tr.com.fkadirogullari.librarymanagementservice.dto.BorrowResponse;
import tr.com.fkadirogullari.librarymanagementservice.exception.ResourceNotFoundException;
import tr.com.fkadirogullari.librarymanagementservice.model.Book;
import tr.com.fkadirogullari.librarymanagementservice.model.Borrow;
import tr.com.fkadirogullari.librarymanagementservice.model.User;
import tr.com.fkadirogullari.librarymanagementservice.publisher.ReactiveBookAvailability;
import tr.com.fkadirogullari.librarymanagementservice.repository.BookRepository;
import tr.com.fkadirogullari.librarymanagementservice.repository.BorrowRepository;
import tr.com.fkadirogullari.librarymanagementservice.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService{

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final HttpServletRequest request;
    private final ReactiveBookAvailability reactiveBookAvailability;


    @Override
    public BorrowResponse borrowBook(String email, Long bookId) {


        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        if (borrowRepository.existsByBookAndReturnedFalse(book)) {
            throw new IllegalStateException("Book is currently not available");
        }

        if (borrowRepository.existsByUserAndReturnedFalse(user)) {
            throw new IllegalStateException("User already has an active borrow");
        }

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(14); // örnek süre

        Borrow borrow = new Borrow();
        borrow.setBook(book);
        borrow.setUser(user);
        borrow.setBorrowDate(borrowDate);
        borrow.setDueDate(dueDate);
        borrow.setReturned(false);

        if (book.getQuantity() <= 0) {
            throw new IllegalStateException("Book is out of stock");
        }

        // Kitabı ödünç veriyoruz → quantity azalt
        book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);

        reactiveBookAvailability.publish(book);

        Borrow saved = borrowRepository.save(borrow);

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

    @Override
    public BorrowResponse returnBook(String email, Long borrowId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        if (!borrow.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You can only return your own borrow");
        }

        if (borrow.isReturned()) {
            throw new IllegalStateException("This borrow has already been returned");
        }

        borrow.setReturned(true);
        borrow.setReturnDate(LocalDate.now());

        Book book = borrow.getBook();
        book.setQuantity(book.getQuantity() + 1);
        bookRepository.save(book);

        reactiveBookAvailability.publish(book);


        Borrow updated = borrowRepository.save(borrow);

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
