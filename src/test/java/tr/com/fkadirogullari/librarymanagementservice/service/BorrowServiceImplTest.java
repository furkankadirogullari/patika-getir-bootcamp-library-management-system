package tr.com.fkadirogullari.librarymanagementservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;


import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

import org.springframework.security.access.AccessDeniedException;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.BorrowResponse;
import tr.com.fkadirogullari.librarymanagementservice.exception.ResourceNotFoundException;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.Book;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.Borrow;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.User;
import tr.com.fkadirogullari.librarymanagementservice.publisher.ReactiveBookAvailability;
import tr.com.fkadirogullari.librarymanagementservice.repository.BookRepository;
import tr.com.fkadirogullari.librarymanagementservice.repository.BorrowRepository;
import tr.com.fkadirogullari.librarymanagementservice.repository.UserRepository;
import tr.com.fkadirogullari.librarymanagementservice.service.Impl.BorrowServiceImpl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class BorrowServiceImplTest {

    @Mock
    private BorrowRepository borrowRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ReactiveBookAvailability reactiveBookAvailability;

    @InjectMocks
    private BorrowServiceImpl borrowService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        borrowService = new BorrowServiceImpl(
                borrowRepository, bookRepository, userRepository, null, reactiveBookAvailability
        );
    }

    @Test
    void borrowBook_successful() {
        // Arrange
        String email = "test@example.com";
        Long bookId = 1L;

        User user = new User();
        user.setId(100L);
        user.setEmail(email);

        Book book = Book.builder()
                .id(bookId)
                .title("Sample Book")
                .quantity(5)
                .build();

        Borrow savedBorrow = new Borrow();
        savedBorrow.setId(10L);
        savedBorrow.setBook(book);
        savedBorrow.setUser(user);
        savedBorrow.setBorrowDate(LocalDate.now());
        savedBorrow.setDueDate(LocalDate.now().plusDays(14));
        savedBorrow.setReturned(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookAndReturnedFalse(book)).thenReturn(false);
        when(borrowRepository.existsByUserAndReturnedFalse(user)).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(borrowRepository.save(any(Borrow.class))).thenReturn(savedBorrow);

        // Act
        BorrowResponse response = borrowService.borrowBook(email, bookId);

        // Assert
        assertNotNull(response);
        assertEquals(bookId, response.getBookId());
        assertEquals(email, response.getUserEmail());
        assertFalse(response.isReturned());
        verify(bookRepository).save(any(Book.class));
        verify(borrowRepository).save(any(Borrow.class));
        verify(reactiveBookAvailability).publish(book);
    }

    @Test
    void borrowBook_shouldThrowException_whenUserNotFound() {
        String email = "unknown@example.com";
        Long bookId = 1L;

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                borrowService.borrowBook(email, bookId)
        );

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void borrowBook_shouldThrowException_whenBookNotFound() {
        String email = "user@example.com";
        Long bookId = 1L;

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User()));
        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () ->
                borrowService.borrowBook(email, bookId)
        );

        assertEquals("Book not found", exception.getMessage());
    }

    @Test
    void borrowBook_shouldThrowException_whenBookAlreadyBorrowed() {
        String email = "user@example.com";
        Long bookId = 1L;

        User user = new User();
        Book book = new Book();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookAndReturnedFalse(book)).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                borrowService.borrowBook(email, bookId)
        );

        assertEquals("Book is currently not available", exception.getMessage());
    }

    @Test
    void borrowBook_shouldThrowException_whenUserHasActiveBorrow() {
        String email = "user@example.com";
        Long bookId = 1L;

        User user = new User();
        Book book = new Book();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookAndReturnedFalse(book)).thenReturn(false);
        when(borrowRepository.existsByUserAndReturnedFalse(user)).thenReturn(true);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                borrowService.borrowBook(email, bookId)
        );

        assertEquals("User already has an active borrow", exception.getMessage());
    }

    @Test
    void borrowBook_shouldThrowException_whenBookIsOutOfStock() {
        String email = "user@example.com";
        Long bookId = 1L;

        User user = new User();
        Book book = new Book();
        book.setQuantity(0);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowRepository.existsByBookAndReturnedFalse(book)).thenReturn(false);
        when(borrowRepository.existsByUserAndReturnedFalse(user)).thenReturn(false);

        IllegalStateException exception = assertThrows(IllegalStateException.class, () ->
                borrowService.borrowBook(email, bookId)
        );

        assertEquals("Book is out of stock", exception.getMessage());
    }

    // --- returnBook başarılı senaryo ---
    @Test
    void returnBook_successful() {
        String email = "user@example.com";
        Long borrowId = 1L;

        User user = new User();
        user.setId(10L);
        user.setEmail(email);

        Book book = Book.builder()
                .id(5L)
                .title("Test Book")
                .quantity(2)
                .build();

        Borrow borrow = new Borrow();
        borrow.setId(borrowId);
        borrow.setUser(user);
        borrow.setBook(book);
        borrow.setBorrowDate(LocalDate.now().minusDays(5));
        borrow.setDueDate(LocalDate.now().plusDays(9));
        borrow.setReturned(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(borrowRepository.findById(borrowId)).thenReturn(Optional.of(borrow));
        when(borrowRepository.save(any(Borrow.class))).thenAnswer(inv -> inv.getArgument(0));
        when(bookRepository.save(any(Book.class))).thenAnswer(inv -> inv.getArgument(0));

        BorrowResponse resp = borrowService.returnBook(email, borrowId);

        assertNotNull(resp);
        assertTrue(resp.isReturned());
        // quantity artmalı
        assertEquals(3, book.getQuantity());
        verify(borrowRepository).save(borrow);
        verify(bookRepository).save(book);
        verify(reactiveBookAvailability).publish(book);
    }

    // --- kullanıcı bulunamazsa ---
    @Test
    void returnBook_shouldThrow_whenUserNotFound() {
        when(userRepository.findByEmail("noone@example.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> borrowService.returnBook("noone@example.com", 1L),
                "User not found"
        );
    }

    // --- borrow kaydı bulunamazsa ---
    @Test
    void returnBook_shouldThrow_whenBorrowNotFound() {
        String email = "user@example.com";
        User user = new User(); user.setId(1L); user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(borrowRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                () -> borrowService.returnBook(email, 99L)
        );
        assertTrue(ex.getMessage().contains("Loan not found"));
    }

    // --- farklı kullanıcı iade etmeye çalışırsa ---
    @Test
    void returnBook_shouldThrow_whenAccessDenied() {
        String email = "user1@example.com";
        User user1 = new User(); user1.setId(1L); user1.setEmail(email);
        User user2 = new User(); user2.setId(2L); user2.setEmail("other@example.com");

        Borrow borrow = new Borrow();
        borrow.setId(50L);
        borrow.setUser(user2);
        borrow.setReturned(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user1));
        when(borrowRepository.findById(50L)).thenReturn(Optional.of(borrow));

        assertThrows(AccessDeniedException.class,
                () -> borrowService.returnBook(email, 50L),
                "You can only return your own borrow"
        );
    }

    // --- zaten iade edilmişse ---
    @Test
    void returnBook_shouldThrow_whenAlreadyReturned() {
        String email = "user@example.com";
        User user = new User(); user.setId(1L); user.setEmail(email);

        Borrow borrow = new Borrow();
        borrow.setId(20L);
        borrow.setUser(user);
        borrow.setReturned(true);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(borrowRepository.findById(20L)).thenReturn(Optional.of(borrow));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> borrowService.returnBook(email, 20L)
        );
        assertTrue(ex.getMessage().contains("already been returned"));
    }

    @Test
    void getUserBorrowHistory_shouldReturnBorrowList_whenUserExists() {
        String email = "test@example.com";
        User user = new User();
        user.setId(1L);
        user.setEmail(email);

        Book book = Book.builder().id(1L).title("Test Book").build();

        Borrow borrow = new Borrow();
        borrow.setId(10L);
        borrow.setUser(user);
        borrow.setBook(book);
        borrow.setBorrowDate(LocalDate.now().minusDays(3));
        borrow.setDueDate(LocalDate.now().plusDays(11));
        borrow.setReturned(false);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(borrowRepository.findAllByUser(user)).thenReturn(List.of(borrow));

        List<BorrowResponse> result = borrowService.getUserBorrowHistory(email);

        assertEquals(1, result.size());
        assertEquals(borrow.getId(), result.get(0).getBorrowId());
        assertEquals("Test Book", result.get(0).getBookTitle());
    }

    @Test
    void getUserBorrowHistory_shouldThrow_whenUserNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> borrowService.getUserBorrowHistory("missing@example.com"));
    }

    @Test
    void getAllBorrowHistory_shouldReturnAllBorrows() {
        User user = new User(); user.setId(1L); user.setEmail("a@a.com");
        Book book = Book.builder().id(2L).title("Book A").build();

        Borrow borrow = new Borrow();
        borrow.setId(1L);
        borrow.setUser(user);
        borrow.setBook(book);
        borrow.setBorrowDate(LocalDate.now().minusDays(1));
        borrow.setDueDate(LocalDate.now().plusDays(13));
        borrow.setReturned(false);

        when(borrowRepository.findAll()).thenReturn(List.of(borrow));

        List<BorrowResponse> all = borrowService.getAllBorrowHistory();

        assertEquals(1, all.size());
        assertEquals("Book A", all.get(0).getBookTitle());
        assertEquals("a@a.com", all.get(0).getUserEmail());
    }

    @Test
    void getOverdueBorrows_shouldReturnOverdueBorrows() {
        User user = new User(); user.setId(1L); user.setEmail("late@user.com");
        Book book = Book.builder().id(2L).title("Late Book").build();

        Borrow borrow = new Borrow();
        borrow.setId(3L);
        borrow.setUser(user);
        borrow.setBook(book);
        borrow.setBorrowDate(LocalDate.now().minusDays(20));
        borrow.setDueDate(LocalDate.now().minusDays(5));
        borrow.setReturned(false);

        when(borrowRepository.findByReturnedFalseAndDueDateBefore(any(LocalDate.class)))
                .thenReturn(List.of(borrow));

        List<BorrowResponse> overdue = borrowService.getOverdueBorrows();

        assertEquals(1, overdue.size());
        assertTrue(overdue.get(0).getDueDate().isBefore(LocalDate.now()));
        assertEquals("Late Book", overdue.get(0).getBookTitle());
    }
}
