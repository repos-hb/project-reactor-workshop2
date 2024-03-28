package code;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

@Slf4j
public class MonoDoOnMethodsTest {

    @Test
    public void testDoOnMethods_emptyMono(){
        Mono.empty()
                .log()
                .doOnSubscribe(s->log.info("inside doOnSubscribe -- {}",s))
                .doOnRequest(s -> log.info("inside doOnRequest -- {}",s))
                .doOnNext(s -> log.info("inside doOnNext -- {}",s))
                .doOnSuccess(s -> log.info("inside doOnSuccess -- {}",s))
                .block();
    }

    @Test
    public void testDoOnMethods_nonEmptyMono(){
        Mono.just("reactor")
                .log()
                .doOnSubscribe(s->log.info("inside doOnSubscribe -- {}",s))
                .doOnRequest(s -> log.info("inside doOnRequest -- {}",s))
                .doOnNext(s -> log.info("inside doOnNext -- {}",s))
                .doOnSuccess(s -> log.info("inside doOnSuccess -- {}",s))
                .block();
    }

    @Test
    public void testDoOnMethods_emptyMonoOfMono(){
        Mono.just(Mono.empty())
                .log()
                .doOnSubscribe(s->log.info("inside doOnSubscribe -- {}",s))
                .doOnRequest(s -> log.info("inside doOnRequest -- {}",s))
                .doOnNext(s -> log.info("inside doOnNext -- {}",s))
                .doOnSuccess(s -> log.info("inside doOnSuccess -- {}",s))
                .block();
    }
}
