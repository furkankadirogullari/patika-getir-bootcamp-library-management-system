package tr.com.fkadirogullari.librarymanagementservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import tr.com.fkadirogullari.librarymanagementservice.dto.BookRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.BookResponse;
import tr.com.fkadirogullari.librarymanagementservice.model.Book;
import tr.com.fkadirogullari.librarymanagementservice.publisher.ReactiveBookAvailability;
import tr.com.fkadirogullari.librarymanagementservice.service.BookService;

import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;
    private final ReactiveBookAvailability publisher;

    @PostMapping
    public BookResponse addBook(@Valid @RequestBody BookRequest request) {
        return bookService.addBook(request);
    }

    @GetMapping("/{isbn}")
    public BookResponse getBookByIsbn(@PathVariable String isbn) {
        return bookService.getBookByIsbn(isbn);
    }

    @GetMapping("/title/{title}")
    public List<BookResponse> getBookByTitle(@PathVariable String title) {
        return bookService.getBookByTitle(title);
    }

    @GetMapping("/author/{author}")
    public List<BookResponse> getBookByAuthor(@PathVariable String author) {
        return bookService.getBookByAuthor(author);
    }

    @GetMapping("/genre/{genre}")
    public List<BookResponse> getBookByGenre(@PathVariable String genre) {
        return bookService.getBookByGenre(genre);
    }

    @GetMapping
    public Page<BookResponse> getAllBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        return bookService.getAllBooks(keyword, pageable);
    }

    @PutMapping("/{isbn}")
    public BookResponse updateBook(@PathVariable String isbn, @Valid @RequestBody BookRequest request) {
        return bookService.updateBook(isbn, request);
    }

    @DeleteMapping("/{isbn}")
    public void deleteBook(@PathVariable String isbn) {
        bookService.deleteBook(isbn);
    }

   /* @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Book> streamBookAvailability() {
        return publisher.getStream();
    }*/
}
