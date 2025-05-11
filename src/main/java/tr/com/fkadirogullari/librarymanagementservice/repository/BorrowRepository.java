package tr.com.fkadirogullari.librarymanagementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.fkadirogullari.librarymanagementservice.model.Book;
import tr.com.fkadirogullari.librarymanagementservice.model.Borrow;
import tr.com.fkadirogullari.librarymanagementservice.model.User;

import java.time.LocalDate;
import java.util.List;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    List<Borrow> findByUser(User user);
    List<Borrow> findByUserAndReturnedFalse(User user);
    boolean existsByBookAndReturnedFalse(Book book);
    boolean existsByUserAndReturnedFalse(User user);
    List<Borrow> findAllByUser(User user);
    List<Borrow> findByReturnedFalseAndDueDateBefore(LocalDate date);
}
