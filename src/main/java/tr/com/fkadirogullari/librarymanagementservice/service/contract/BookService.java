package tr.com.fkadirogullari.librarymanagementservice.service.contract;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.BookRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.response.BookResponse;

import java.util.List;

public interface BookService {
    BookResponse addBook(BookRequest request);

    BookResponse getBookByIsbn(String isbn);

    List<BookResponse> getBookByTitle(String title);

    List<BookResponse> getBookByAuthor(String author);

    List<BookResponse> getBookByGenre(String genre);

    Page<BookResponse> getAllBooks(String keyword, Pageable pageable);

    BookResponse updateBook(String isbn, BookRequest request);

    void deleteBook(String isbn);


}
