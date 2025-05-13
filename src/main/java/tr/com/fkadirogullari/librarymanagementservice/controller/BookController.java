package tr.com.fkadirogullari.librarymanagementservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.BookRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.response.BookResponse;
import tr.com.fkadirogullari.librarymanagementservice.publisher.ReactiveBookAvailability;
import tr.com.fkadirogullari.librarymanagementservice.service.contract.BookService;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final ReactiveBookAvailability publisher;

    // Creates a new book (Only accessible by librarians)
    @PostMapping
    public BookResponse addBook(@Valid @RequestBody BookRequest request) {
        return bookService.addBook(request);
    }


    // Searches for a book by its ISBN
    @GetMapping("/{isbn}")
    public BookResponse getBookByIsbn(@PathVariable String isbn) {
        return bookService.getBookByIsbn(isbn);
    }


    // Searches for a book by its title
    @GetMapping("/title/{title}")
    public List<BookResponse> getBookByTitle(@PathVariable String title) {
        return bookService.getBookByTitle(title);
    }


    // Searches for a book by its author
    @GetMapping("/author/{author}")
    public List<BookResponse> getBookByAuthor(@PathVariable String author) {
        return bookService.getBookByAuthor(author);
    }


    // Searches for a book by its genre
    @GetMapping("/genre/{genre}")
    public List<BookResponse> getBookByGenre(@PathVariable String genre) {
        return bookService.getBookByGenre(genre);
    }


    // Retrieves a list of all books (Accessible to authenticated users)
    @GetMapping
    public Page<BookResponse> getAllBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return bookService.getAllBooks(keyword, pageable);
    }

    // Updates a book's details by ID (Only librarians can perform this)
    @PutMapping("/{isbn}")
    public BookResponse updateBook(@PathVariable String isbn, @Valid @RequestBody BookRequest request) {
        return bookService.updateBook(isbn, request);
    }

    // Deletes a book by ID (Only accessible to librarians)
    @DeleteMapping("/{isbn}")
    public ResponseEntity<Void> deleteBook(@PathVariable String isbn) {

        bookService.deleteBook(isbn);
        return ResponseEntity.noContent().build();
    }


}
