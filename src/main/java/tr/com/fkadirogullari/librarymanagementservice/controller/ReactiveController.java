package tr.com.fkadirogullari.librarymanagementservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import tr.com.fkadirogullari.librarymanagementservice.publisher.ReactiveBookAvailability;
import tr.com.fkadirogullari.librarymanagementservice.model.entity.Book;


@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
// REST controller for reactive, real-time updates using Spring WebFlux
public class ReactiveController {

    private final ReactiveBookAvailability publisher;

    // Streams real-time book availability updates using Server-Sent Events (SSE)
    @Operation(
            summary = "real-time book availability update",
            description = "Displays real-time changing book information using Spring WebFlux."
    )
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Book> streamBookAvailability() {

        // Returns a Flux<Book> stream that emits updates when book availability changes
        return publisher.getStream();
    }
}
