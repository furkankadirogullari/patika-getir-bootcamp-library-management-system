package tr.com.fkadirogullari.librarymanagementservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tr.com.fkadirogullari.librarymanagementservice.dto.BookRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.BookResponse;
import tr.com.fkadirogullari.librarymanagementservice.model.Book;

import java.util.List;

public interface BookService {
    BookResponse addBook(BookRequest request);

    BookResponse getBookByIsbn(String isbn);

    List<BookResponse> getBookByTitle(String title);

    List<BookResponse> getBookByAuthor(String author);

    List<BookResponse> getBookByGenre(String genre);

    //BookResponse getBookByTitle(String title);

    //BookResponse getBookByAuthor(String author);

    //BookResponse getBookByGenre(String genre);

    Page<BookResponse> getAllBooks(String keyword, Pageable pageable);

    BookResponse updateBook(String isbn, BookRequest request);

    void deleteBook(String isbn);


}
