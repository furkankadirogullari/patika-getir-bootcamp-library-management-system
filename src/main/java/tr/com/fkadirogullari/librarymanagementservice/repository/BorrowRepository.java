package tr.com.fkadirogullari.librarymanagementservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tr.com.fkadirogullari.librarymanagementservice.model.Borrow;
import tr.com.fkadirogullari.librarymanagementservice.model.User;

import java.util.List;

public interface BorrowRepository extends JpaRepository<Borrow, Long> {

    List<Borrow> findByUser(User user);
    List<Borrow> findByUserAndReturnedFalse(User user);
}
