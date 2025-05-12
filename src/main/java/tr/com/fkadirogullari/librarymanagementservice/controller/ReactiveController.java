package tr.com.fkadirogullari.librarymanagementservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import tr.com.fkadirogullari.librarymanagementservice.publisher.ReactiveBookAvailability;
import tr.com.fkadirogullari.librarymanagementservice.model.Book;


@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class ReactiveController {

    private final ReactiveBookAvailability publisher;

    @Operation(
            summary = "real-time book availability update",
            description = "Displays real-time changing book information using Spring WebFlux."
    )
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Book> streamBookAvailability() {
        return publisher.getStream();
    }
}
