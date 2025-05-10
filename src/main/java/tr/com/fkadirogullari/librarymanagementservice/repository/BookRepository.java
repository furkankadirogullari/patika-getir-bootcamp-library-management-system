package tr.com.fkadirogullari.librarymanagementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tr.com.fkadirogullari.librarymanagementservice.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book,Long>, JpaSpecificationExecutor<Book> {

    Optional<Book> findByIsbn(String isbn);
    void deleteByIsbn(String isbn);

}
