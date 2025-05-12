package tr.com.fkadirogullari.librarymanagementservice.service.Impl;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.request.BookRequest;
import tr.com.fkadirogullari.librarymanagementservice.model.dto.response.BookResponse;
import tr.com.fkadirogullari.librarymanagementservice.exception.ResourceNotFoundException;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.Book;
import tr.com.fkadirogullari.librarymanagementservice.repository.BookRepository;
import tr.com.fkadirogullari.librarymanagementservice.service.contract.BookService;


import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    // Saves a new book to the database
    @Override
    public BookResponse addBook(BookRequest request) {

        // Check if a book with the same ISBN already exists
        Optional<Book> existingBook = bookRepository.findByIsbn(request.getIsbn());
        if (existingBook.isPresent()) {
            throw new IllegalArgumentException("Book with this ISBN already exists.");
        }

        // Create and save a new Book entity
        Book book = Book.builder()
                .title(request.getTitle())
                .author(request.getAuthor())
                .isbn(request.getIsbn())
                .publicationDate(request.getPublicationDate())
                .genre(request.getGenre())
                .quantity(request.getQuantity())
                .build();

        // Return the saved book as a response DTO
        return mapToResponse(bookRepository.save(book));
    }

    //Get book by ISBN
    @Override
    public BookResponse getBookByIsbn(String isbn) {

        // Retrieve a book by its ISBN or throw exception if not found
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ISBN: " + isbn));
        return mapToResponse(book);
    }

    //Get books by Title
    @Override
    public List<BookResponse> getBookByTitle(String title) {

        // Retrieve all books with the given title
        return bookRepository.findAllByTitle(title).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    //Get books by Genre
    @Override
    public List<BookResponse> getBookByGenre(String genre) {

        // Retrieve all books with the given genre
        return bookRepository.findAllByGenre(genre).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    //Get books by Author
    @Override
    public List<BookResponse> getBookByAuthor(String author) {

        // Retrieve all books written by the given author
        return bookRepository.findAllByAuthor(author).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }


    @Override
    public Page<BookResponse> getAllBooks(String keyword, Pageable pageable) {
        Page<Book> books;

        // If a keyword is provided, perform a dynamic search by title, author, ISBN, or genre
        if (keyword != null && !keyword.isBlank()) {
            books = bookRepository.findAll((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("title")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("author")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("isbn")), "%" + keyword.toLowerCase() + "%"),
                    cb.like(cb.lower(root.get("genre")), "%" + keyword.toLowerCase() + "%")
            ), pageable);
        } else {
            // If no keyword, return all books paginated
            books = bookRepository.findAll(pageable);
        }

        return books.map(this::mapToResponse);
    }

    //Update book by isbn
    @Override
    public BookResponse updateBook(String isbn, BookRequest request) {

        // Find the book to update by ISBN or throw exception
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ISBN: " + isbn));

        // Update the book details
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setPublicationDate(request.getPublicationDate());
        book.setGenre(request.getGenre());

        // Save and return the updated book
        return mapToResponse(bookRepository.save(book));
    }

    //Delete book by isbn
    @Override
    public void deleteBook(String isbn) {
        // Find and delete the book by ISBN
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with ISBN: " + isbn));
        bookRepository.delete(book);
    }

    // Converts Book entity to BookResponse DTO
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
