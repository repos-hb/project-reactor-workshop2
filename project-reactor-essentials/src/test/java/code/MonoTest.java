package code;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
public class MonoTest {

    @Test
    public void monoSubscriber() {
        String name = "William Suane";
        Mono<String> mono = Mono.just(name)
                .log();

        mono.subscribe();
        log.info("--------------------------");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer(){
        String name = "William Suane";
        Mono<String> mono = Mono.just(name)
                .log();

        // Params: consumer – the consumer to invoke on each value (onNext signal)
        mono.subscribe(log::info);
        log.info("--------------------------");
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerError(){
        String name = "William Suane";
        Mono<String> mono = Mono.just(name)
                .map(s -> {throw new RuntimeException("Testing mono with error!");});

        mono.subscribe(s->log.info("Name: "+s), s->log.error("ERROR HAPPENED!"));
        mono.subscribe(s -> log.info("Name {}", s), Throwable::printStackTrace);
        log.info("--------------------------");
        StepVerifier.create(mono)
                .expectError()
                .verify();
    }
}
