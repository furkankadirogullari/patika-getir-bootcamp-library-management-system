package tr.com.fkadirogullari.librarymanagementservice.model;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "books")
@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    private int quantity;

    private String author;

    @Column(nullable = false, unique = true)
    private String isbn;

    private LocalDate publicationDate;

    private String genre;
}
