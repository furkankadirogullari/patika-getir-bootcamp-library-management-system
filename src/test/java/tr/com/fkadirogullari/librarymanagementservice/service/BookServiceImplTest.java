package tr.com.fkadirogullari.librarymanagementservice.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import tr.com.fkadirogullari.librarymanagementservice.dto.BookRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.BookResponse;
import tr.com.fkadirogullari.librarymanagementservice.exception.ResourceNotFoundException;
import tr.com.fkadirogullari.librarymanagementservice.model.Book;
import tr.com.fkadirogullari.librarymanagementservice.repository.BookRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    // 1. Test: addBook - Book successfully added
    @Test
    void testAddBook_Success() {
        // Arrange
        BookRequest request = new BookRequest("Title", "Author", "123456789", "Genre", LocalDate.now(), 10);
        Book book = new Book(1L, "Title", 10, "Author", "123456789", LocalDate.now(),"Genre");
        Mockito.when(bookRepository.findByIsbn(request.getIsbn())).thenReturn(Optional.empty());
        Mockito.when(bookRepository.save(Mockito.any(Book.class))).thenReturn(book);

        // Act
        BookResponse response = bookService.addBook(request);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals("Title", response.getTitle());
        Assertions.assertEquals("Author", response.getAuthor());
    }

    // 2. Test: addBook - Book with the same ISBN already exists
    @Test
    void testAddBook_AlreadyExists() {
        // Arrange
        BookRequest request = new BookRequest("Title", "Author", "123456789", "Genre", LocalDate.now(), 10);
        Book existingBook = new Book(1L, "Title", 10, "Author", "123456789", LocalDate.now(), "Genre");
        Mockito.when(bookRepository.findByIsbn(request.getIsbn())).thenReturn(Optional.of(existingBook));

        // Act & Assert
        Assertions.assertThrows(IllegalArgumentException.class, () -> bookService.addBook(request));
    }

    // 3. Test: getBookByIsbn - Book found
    @Test
    void testGetBookByIsbn_Success() {
        // Arrange
        String isbn = "123456789";
        Book book = new Book(1L, "Title", 10, "Author", isbn, LocalDate.now(), "Genre");
        Mockito.when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(book));

        // Act
        BookResponse response = bookService.getBookByIsbn(isbn);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals(isbn, response.getIsbn());
        Assertions.assertEquals("Title", response.getTitle());
    }

    // 4. Test: getBookByIsbn - Book not found
    @Test
    void testGetBookByIsbn_NotFound() {
        // Arrange
        String isbn = "123456789";
        Mockito.when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        // Act & Assert
        Assertions.assertThrows(ResourceNotFoundException.class, () -> bookService.getBookByIsbn(isbn));
    }

    // 5. Test: getBookByTitle
    @Test
    void testGetBookByTitle() {
        // Arrange
        String title = "Title";
        Book book = new Book(1L, title, 10, "Author", "123456789", LocalDate.now(), "Genre");
        Mockito.when(bookRepository.findAllByTitle(title)).thenReturn(List.of(book));

        // Act
        List<BookResponse> response = bookService.getBookByTitle(title);

        // Assert
        Assertions.assertEquals(1, response.size());
        Assertions.assertEquals(title, response.get(0).getTitle());
    }

    // 6. Test: updateBook - Book updated
    @Test
    void testUpdateBook_Success() {
        // Arrange
        String isbn = "123456789";
        BookRequest request = new BookRequest("Updated Title", "Updated Author", isbn, "Genre", LocalDate.now(), 5);
        Book existingBook = new Book(1L, "Old Title", 5, "Old Author", isbn, LocalDate.now(), "Old Genre");
        Mockito.when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(existingBook));
        Mockito.when(bookRepository.save(Mockito.any(Book.class))).thenReturn(existingBook);

        // Act
        BookResponse response = bookService.updateBook(isbn, request);

        // Assert
        Assertions.assertNotNull(response);
        Assertions.assertEquals("Updated Title", response.getTitle());
        Assertions.assertEquals("Updated Author", response.getAuthor());
    }

    // 7. Test: deleteBook - Book deleted
    @Test
    void testDeleteBook_Success() {
        // Arrange
        String isbn = "123456789";
        Book existingBook = new Book(1L, "Title", 5, isbn, isbn, LocalDate.now(), "Genre");
        Mockito.when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(existingBook));

        // Act
        bookService.deleteBook(isbn);

        // Assert
        Mockito.verify(bookRepository, Mockito.times(1)).delete(existingBook);
    }

    @Test
    void testGetBookByGenre() {
        String genre = "Fantasy";
        Book book = new Book(1L, "Title", 10,"Author" , "123456789", LocalDate.now(), genre);
        Mockito.when(bookRepository.findAllByGenre(genre)).thenReturn(List.of(book));

        List<BookResponse> responses = bookService.getBookByGenre(genre);

        Assertions.assertEquals(1, responses.size());
        Assertions.assertEquals(genre, responses.get(0).getGenre());
    }

    @Test
    void testGetBookByAuthor() {
        String author = "J.R.R. Tolkien";
        Book book = new Book(1L, "The Hobbit", 7, author, "123456789", LocalDate.of(1937,9,21), "Fantasy");
        Mockito.when(bookRepository.findAllByAuthor(author)).thenReturn(List.of(book));

        List<BookResponse> responses = bookService.getBookByAuthor(author);

        Assertions.assertEquals(1, responses.size());
        Assertions.assertEquals(author, responses.get(0).getAuthor());
    }

    @Test
    void testGetAllBooks_WithKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Book book = new Book(1L, "Spring in Action",5 , "Craig Walls", "11111111111", LocalDate.of(2020,1,1), "Programming");
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        Mockito.when(bookRepository.findAll(Mockito.any(Specification.class), Mockito.eq(pageable)))
                .thenReturn(bookPage);

        Page<BookResponse> result = bookService.getAllBooks("spring", pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("Spring in Action", result.getContent().get(0).getTitle());
    }

    @Test
    void testGetAllBooks_WithoutKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Book book = new Book(1L, "Clean Code", 3, "Robert C. Martin", "123456789", LocalDate.of(2008,8,1), "Code");
        Page<Book> bookPage = new PageImpl<>(List.of(book));

        Mockito.when(bookRepository.findAll(pageable)).thenReturn(bookPage);

        Page<BookResponse> result = bookService.getAllBooks(null, pageable);

        Assertions.assertEquals(1, result.getTotalElements());
        Assertions.assertEquals("Clean Code", result.getContent().get(0).getTitle());
    }

    @Test
    void testUpdateBook_BookNotFound() {
        String isbn = "999999999";
        BookRequest request = new BookRequest("Title", "Author", isbn, "Genre", LocalDate.of(2024,1,1), 5);
        Mockito.when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> bookService.updateBook(isbn, request));
    }

    @Test
    void testDeleteBook_BookNotFound() {
        String isbn = "000000000";
        Mockito.when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        Assertions.assertThrows(ResourceNotFoundException.class, () -> bookService.deleteBook(isbn));
    }
}
