package code;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

@Slf4j
public class OperatorTest {

    @Test
    public void testSubscribeOn(){
        Flux<Integer> flux = Flux.range(1, 5)
                .map(i -> {
                    log.info("Map1: Thread {}: Data{}", Thread.currentThread().getName(), i);
                    return i;
                })
                .subscribeOn(Schedulers.immediate())    // no-op Scheduler, submitted Runnable will be directly executed on current thread [main]
                .map(i -> {
                    log.info("Map2: Thread {}: Data{}", Thread.currentThread().getName(), i);
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4,5)
                .expectComplete()
                .verify();
    }

    @Test
    public void testSubscribeOn2(){
        Flux<Integer> flux = Flux.range(1, 5)
                .map(i -> {
                    log.info("Map1: Thread {}: Data{}", Thread.currentThread().getName(), i);
                    return i;
                })
                .subscribeOn(Schedulers.single())    // A single, reusable thread, reuses the same thread for all callers, until the Scheduler is disposed.
                .map(i -> {
                    log.info("Map2: Thread {}: Data{}", Thread.currentThread().getName(), i);
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4,5)
                .expectComplete()
                .verify();
    }

    @Test
    public void testSubscribeOn3(){
        Flux<Integer> flux = Flux.range(1, 5)
                .map(i -> {
                    log.info("Map1: Thread {}: Data{}", Thread.currentThread().getName(), i);
                    return i;
                })
                .subscribeOn(Schedulers.newSingle("worker"))    // per-call dedicated thread
                .map(i -> {
                    log.info("Map2: Thread {}: Data{}", Thread.currentThread().getName(), i);
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4,5)
                .expectComplete()
                .verify();
    }

    @Test
    public void testSubscribeOn4(){
        Flux<Integer> flux = Flux.range(1, 5)
                .map(i -> {
                    log.info("Map1: Thread {}: Data{}", Thread.currentThread().getName(), i);
                    return i;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map2: Thread {}: Data{}", Thread.currentThread().getName(), i);
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4,5)
                .expectComplete()
                .verify();
    }

    @Test
    public void testPublishOn(){
        Flux<Integer> flux = Flux.range(1, 5)
                .map(i -> {
                    log.info("Map1: Thread {}: Data{}", Thread.currentThread().getName(), i);
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map2: Thread {}: Data{}", Thread.currentThread().getName(), i);
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1,2,3,4,5)
                .expectComplete()
                .verify();
    }

    @Test
    public void testSwitchIfEmpty(){
        Flux<Object> flux = Flux.empty()
                .switchIfEmpty(Flux.just("not empty"));

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("not empty")
                .expectComplete()
                .verify();
    }

    @Test
    public void testDefer() throws InterruptedException {
        Flux<Long> just = Flux.just(System.currentTimeMillis());

        just.subscribe(ms->log.info("{}",ms));
        Thread.sleep(100);
        just.subscribe(ms->log.info("{}",ms));
        Thread.sleep(100);
        just.subscribe(ms->log.info("{}",ms));
        Thread.sleep(100);
        just.subscribe(ms->log.info("{}",ms));

        log.info("-----------");

        // defer executes the given supplier every time a subscription happens
        Flux<Long> defer = Flux.defer(() -> Flux.just(System.currentTimeMillis()));

        defer.subscribe(ms->log.info("{}",ms));
        Thread.sleep(100);
        defer.subscribe(ms->log.info("{}",ms));
        Thread.sleep(100);
        defer.subscribe(ms->log.info("{}",ms));
        Thread.sleep(100);
        defer.subscribe(ms->log.info("{}",ms));
    }

    @Test
    public void testConcat(){
        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> concatFlux = Flux.concat(flux1, flux2).log();

        StepVerifier.create(concatFlux)
                .expectSubscription()
                .expectNext("a", "b","c", "d")
                .verifyComplete();
    }

    @Test
    public void testConcatWith(){
        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> concatFlux = flux1.concatWith(flux2).log();

        StepVerifier.create(concatFlux)
                .expectSubscription()
                .expectNext("a", "b","c", "d")
                .verifyComplete();
    }

    @Test
    public void testCombineLatest(){
        Flux<String> flux1 = Flux.just("a", "b");
        Flux<String> flux2 = Flux.just("c", "d");

        // Build a Flux whose data are generated by the combination of the
        // most recently published value from each of two Publisher sources.
        Flux<String> combinedFlux = Flux.combineLatest(flux1, flux2, (s, s2) -> s.concat(s2));

        StepVerifier.create(combinedFlux)
                .expectSubscription()
                .expectNext("bc", "bd")
                .verifyComplete();
    }
}
