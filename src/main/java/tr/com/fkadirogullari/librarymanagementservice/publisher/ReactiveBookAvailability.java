package tr.com.fkadirogullari.librarymanagementservice.publisher;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import tr.com.fkadirogullari.librarymanagementservice.model.Book;

@Component
public class ReactiveBookAvailability {

    private final Sinks.Many<Book> sink = Sinks.many().multicast().onBackpressureBuffer();

    public void publish(Book book) {
        sink.tryEmitNext(book);
    }

    public Flux<Book> getStream() {
        return sink.asFlux();
    }
}
