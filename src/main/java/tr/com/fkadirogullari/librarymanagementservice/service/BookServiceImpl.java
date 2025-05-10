package tr.com.fkadirogullari.librarymanagementservice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tr.com.fkadirogullari.librarymanagementservice.dto.BookRequest;
import tr.com.fkadirogullari.librarymanagementservice.dto.BookResponse;
import tr.com.fkadirogullari.librarymanagementservice.exception.ResourceNotFoundException;
import tr.com.fkadirogullari.librarymanagementservice.model.Book;
import tr.com.fkadirogullari.librarymanagementservice.repository.BookRepository;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService{

    private final BookRepository bookRepository;

    @Override
    public BookResponse addBook(BookRequest request) {
        Optional<Book> existingBook = bookRepository.findByIsbn(request.getIsbn());
        if (existingBook.isPresent()) {
            throw new IllegalArgumentException("Book with this ISBN already exists.");
        }

        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publicationDate(request.getPublicationDate())
                .genre(request.getGenre())
                .quantity(request.getQuantity())
                .build();

        return mapToResponse(bookRepository.save(book));
    }

    @Override
    public BookResponse getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ISBN: " + isbn));
        return mapToResponse(book);
    }

    @Override
    public List<BookResponse> getBookByTitle(String title) {
        return bookRepository.findAllByTitle(title).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponse> getBookByGenre(String genre) {
        return bookRepository.findAllByGenre(genre).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookResponse> getBookByAuthor(String author) {
        return bookRepository.findAllByAuthor(author).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<BookResponse> getAllBooks(String keyword, Pageable pageable) {
        Page<Book> books;
        if (keyword != null && !keyword.isBlank()) {
            books = bookRepository.findAll((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("author")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("isbn")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("genre")), "%" + keyword.toLowerCase() + "%")
            ), pageable);
        } else {
            books = bookRepository.findAll(pageable);
        }

        return books.map(this::mapToResponse);
    }

    @Override
    public BookResponse updateBook(String isbn, BookRequest request) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ISBN: " + isbn));

        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublicationDate(request.getPublicationDate());
        book.setGenre(request.getGenre());

        return mapToResponse(bookRepository.save(book));
    }

    @Override
    public void deleteBook(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ISBN: " + isbn));
        bookRepository.delete(book);
    }

    private BookResponse mapToResponse(Book book) {
        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .publicationDate(book.getPublicationDate())
                .genre(book.getGenre())
                .quantity(book.getQuantity())
                .build();
    }
}
