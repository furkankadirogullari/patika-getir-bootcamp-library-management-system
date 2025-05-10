package tr.com.fkadirogullari.librarymanagementservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tr.com.fkadirogullari.librarymanagementservice.dto.BookRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.BookResponse;

public interface BookService {
    BookResponse addBook(BookRequest request);

    BookResponse getBookByIsbn(String isbn);

    Page<BookResponse> getAllBooks(String keyword, Pageable pageable);

    BookResponse updateBook(String isbn, BookRequest request);

    void deleteBook(String isbn);
}
