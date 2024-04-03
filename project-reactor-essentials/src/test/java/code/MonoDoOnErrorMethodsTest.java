package code;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@Slf4j
public class MonoDoOnErrorMethodsTest {

    @Test
    public void testDoOnError(){
        Mono<Object> errorMono = Mono.error(new IllegalArgumentException("error from mono"))
                .doOnError(throwable -> log.error(throwable.getMessage()))
                .doOnNext(s->log.info("handling error from mono"))      // ignored
                .log();

        StepVerifier.create(errorMono)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    public void testOnErrorResume(){
        String data = "new mono";
        Mono<Object> errorMono = Mono.error(new IllegalArgumentException("error from mono"))
                .onErrorResume(throwable -> {
                    log.info("handling error from onErrorResume");
                    return Mono.just(data);
                })
                .doOnError(throwable -> {       // ignored
                    log.info("handling error from doOnError");
                    log.error(throwable.getMessage());
                })
                .doOnNext(s->log.info("executing after handling error"))
                .log();

        StepVerifier.create(errorMono)
                .expectNext(data)
                .verifyComplete();
    }

    @Test
    public void testOnErrorReturn(){
        String data = "new mono";
        Mono<Object> errorMono = Mono.error(new IllegalArgumentException("error from mono"))
                .onErrorReturn(data)
                .doOnNext(s->log.info("executing after handling error"))
                .log();

        StepVerifier.create(errorMono)
                .expectNext(data)
                .verifyComplete();
    }
}
