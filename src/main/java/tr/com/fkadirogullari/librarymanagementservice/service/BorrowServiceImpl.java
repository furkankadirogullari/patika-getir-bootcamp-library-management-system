package tr.com.fkadirogullari.librarymanagementservice.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tr.com.fkadirogullari.librarymanagementservice.dto.BorrowRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.BorrowResponse;
import tr.com.fkadirogullari.librarymanagementservice.exception.ResourceNotFoundException;
import tr.com.fkadirogullari.librarymanagementservice.model.Book;
import tr.com.fkadirogullari.librarymanagementservice.model.Borrow;
import tr.com.fkadirogullari.librarymanagementservice.model.User;
import tr.com.fkadirogullari.librarymanagementservice.repository.BookRepository;
import tr.com.fkadirogullari.librarymanagementservice.repository.BorrowRepository;
import tr.com.fkadirogullari.librarymanagementservice.repository.UserRepository;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BorrowServiceImpl implements BorrowService{

    private final BorrowRepository borrowRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final HttpServletRequest request;

    @Override
    public BorrowResponse borrowBook(BorrowRequest req) {
        Book book = bookRepository.findById(req.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));

        /*if (book.getQuantity() <= 0) {
            throw new IllegalStateException("Book is not available for borrow");
        }*/

        User user = getAuthenticatedUser();

        //book.setQuantity(book.getQuantity() - 1);
        bookRepository.save(book);

        Borrow borrow = Borrow.builder()
                .book(book)
                .user(user)
                .borrowDate(LocalDateTime.now())
                .returned(false)
                .build();

        return mapToResponse(borrowRepository.save(borrow));
    }

    @Override
    public BorrowResponse returnBook(Long borrowId) {
        Borrow borrow = borrowRepository.findById(borrowId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow not found"));

        if (borrow.isReturned()) {
            throw new IllegalStateException("Book already returned");
        }

        borrow.setReturned(true);
        borrow.setReturnDate(LocalDateTime.now());

        // Book quantity artırılır
        //Book book = borrow.getBook();
        //book.setQuantity(book.getQuantity() + 1);
        //bookRepository.save(book);

        return mapToResponse(borrowRepository.save(borrow));
    }

    public List<BorrowResponse> getMyBorrows(boolean onlyActive) {
        User user = getAuthenticatedUser();
        List<Borrow> borrows = onlyActive
                ? borrowRepository.findByUserAndReturnedFalse(user)
                : borrowRepository.findByUser(user);

        return borrows.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private User getAuthenticatedUser() {
        Principal principal = request.getUserPrincipal();
        String email = principal.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private BorrowResponse mapToResponse(Borrow borrow) {
        return BorrowResponse.builder()
                .id(borrow.getId())
                .bookId(borrow.getBook().getId())
                .bookTitle(borrow.getBook().getTitle())
                .borrowDate(borrow.getBorrowDate())
                .returnDate(borrow.getReturnDate())
                .returned(borrow.isReturned())
                .build();
    }
}
