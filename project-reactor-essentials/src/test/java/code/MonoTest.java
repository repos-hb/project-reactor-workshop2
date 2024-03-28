package code;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscription;
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

        // Params 1: consumer – the consumer to invoke on each next signal
        // 2: errorConsumer – the consumer to invoke on error signal
        mono.subscribe(s->log.info("Name: "+s), s->log.error("ERROR HAPPENED!"));
        mono.subscribe(s -> log.info("Name {}", s), Throwable::printStackTrace);
        log.info("--------------------------");
        StepVerifier.create(mono)
                .expectError()
                .verify();
    }

    @Test
    public void monoSubscriberConsumerErrorComplete(){
        String name = "William Suane";
        Mono<String> mono = Mono.just(name).log()
                .map(String::toUpperCase);

        // Params 1: consumer – the consumer to invoke on each next signal
        // 2: errorConsumer – the consumer to invoke on error signal
        // 3: Runnable completeConsumer - the consumer to invoke on complete signal
        mono.subscribe(s->log.info("Name: "+s),
                s->log.error("ERROR HAPPENED!"),
                () -> log.info("FINISHED!!"));

        log.info("--------------------------");
        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerErrorCompleteSubscription(){
        String name = "William Suane";
        Mono<String> mono = Mono.just(name).log()
                .map(String::toUpperCase);

        // Params 1: Consumer consumer – the consumer to invoke on each next signal
        // 2: Consumer errorConsumer – the consumer to invoke on error signal
        // 3: Runnable completeConsumer - the consumer to invoke on complete signal
        // 4: Consumer subscriptionConsumer - the consumer to invoke on subscribe signal
        mono.subscribe(s->log.info("Name: "+s),
                s->log.error("ERROR HAPPENED!"),
                () -> log.info("FINISHED!!"),
                Subscription::cancel);

        log.info("--------------------------");
        mono.subscribe(s->log.info("Name: "+s),
                s->log.error("ERROR HAPPENED!"),
                () -> log.info("FINISHED!!"),
                subscription -> log.info("on subscribe: "+subscription.toString()));

        log.info("--------------------------");
        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }


}
