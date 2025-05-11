package tr.com.fkadirogullari.librarymanagementservice.service;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import tr.com.fkadirogullari.librarymanagementservice.dto.BorrowRequest;
import tr.com.fkadirogullari.librarymanagementservice.exception.ResourceNotFoundException;
import tr.com.fkadirogullari.librarymanagementservice.model.Book;
import tr.com.fkadirogullari.librarymanagementservice.model.Borrow;
import tr.com.fkadirogullari.librarymanagementservice.model.User;
import tr.com.fkadirogullari.librarymanagementservice.repository.BookRepository;
import tr.com.fkadirogullari.librarymanagementservice.repository.BorrowRepository;
import tr.com.fkadirogullari.librarymanagementservice.repository.UserRepository;

import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class BorrowServiceImplTest {

    @InjectMocks
    private BorrowServiceImpl borrowService;

    @Mock
    private BookRepository bookRepository;
    @Mock
    private BorrowRepository loanRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private Principal principal;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void borrowBook_shouldSucceed() {
        // Arrange
        BorrowRequest request = new BorrowRequest();
        request.setBookId(1L);

        Book book = Book.builder().id(1L).title("Test Book").quantity(5).build();
        User user = User.builder().id(1L).email("user@example.com").build();

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(httpServletRequest.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(loanRepository.save(any())).thenAnswer(invocation -> {
            Borrow borrow = invocation.getArgument(0);
            borrow.setId(1L);
            borrow.setBorrowDate(LocalDate.now());
            return borrow;
        });

        // Act
        var response = borrowService.borrowBook(request);

        // Assert
        assertNotNull(response);
        assertEquals("Test Book", response.getBookTitle());
        assertFalse(response.isReturned());
        verify(bookRepository).save(any(Book.class));
        verify(loanRepository).save(any(Borrow.class));
    }

    @Test
    void borrowBook_whenBookNotFound_shouldThrowException() {
        // Arrange
        BorrowRequest request = new BorrowRequest();
        request.setBookId(99L);

        when(bookRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            borrowService.borrowBook(request);
        });

        assertEquals("Book not found", exception.getMessage());
    }

    @Test
    void borrowBook_whenBookOutOfStock_shouldThrowException() {
        // Arrange
        BorrowRequest request = new BorrowRequest();
        request.setBookId(1L);

        Book book = Book.builder().id(1L).title("Unavailable Book").quantity(0).build();
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            borrowService.borrowBook(request);
        });

        assertEquals("Book is not available for loan", exception.getMessage());
    }

    @Test
    void returnBook_whenAlreadyReturned_shouldThrowException() {
        // Arrange
        Borrow loan = Borrow.builder()
                .id(1L)
                .returned(true)
                .build();

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        // Act & Assert
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            borrowService.returnBook(1L);
        });

        assertEquals("Book already returned", exception.getMessage());
    }

    @Test
    void returnBook_shouldSucceed() {
        // Arrange
        Book book = Book.builder().id(1L).title("Book").quantity(2).build();
        Borrow loan = Borrow.builder().id(1L).book(book).returned(false).build();

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(bookRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        var response = borrowService.returnBook(1L);

        // Assert
        assertTrue(response.isReturned());
        assertNotNull(response.getReturnDate());
        verify(bookRepository).save(any());
    }
}
