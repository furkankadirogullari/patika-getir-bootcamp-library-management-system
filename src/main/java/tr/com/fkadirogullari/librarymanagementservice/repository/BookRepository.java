package tr.com.fkadirogullari.librarymanagementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book,Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findByIsbn(String isbn);
    List<Book> findAllByTitle(String title);
    List<Book> findAllByGenre(String genre);
    List<Book> findAllByAuthor(String author);
    void deleteByIsbn(String isbn);

}
