package code;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.time.Duration;

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
    public void testConcatDelayError(){
        Flux<String> flux1 = Flux.just("a", "b")
                .map(s -> {
                    if("b".equals(s))
                        throw new IllegalArgumentException();
                    return s;
                }).doOnError(t->log.error("something bad happened"));

        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> concatFlux = Flux.concatDelayError(flux1, flux2);

        StepVerifier.create(concatFlux)
                .expectSubscription()
                .expectNext("a","c", "d")
                .expectError()
                .verify();
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

    @Test
    public void testMerge() throws InterruptedException {
        Flux<String> flux1 = Flux.just("a", "b").delayElements(Duration.ofMillis(200));
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> fluxMerged = Flux.merge(flux1, flux2).log();

        // Obs: running in different threads
        fluxMerged.subscribe(log::info);

        Thread.sleep(1000);

        StepVerifier.create(fluxMerged)
                .expectSubscription()
                .expectNext("c","d","a","b")
                .verifyComplete();

    }

    @Test
    public void differenceBetweenConcatAndMerge() throws InterruptedException {
        // interleaved emissions
        Flux<String> flux1 = Flux.just("a", "b").delayElements(Duration.ofMillis(200));
        Flux<String> flux2 = Flux.just("c", "d").delayElements(Duration.ofMillis(100));

        Flux<String> fluxMerged = Flux.merge(flux1, flux2).log();

        StepVerifier.create(fluxMerged)
                .expectSubscription()
                .expectNext("c","a","d","b")
                .verifyComplete();

        Flux<String> fluxConcat = Flux.concat(flux1, flux2);

        StepVerifier.create(fluxConcat)
                .expectSubscription()
                .expectNext("a","b","c","d")
                .verifyComplete();

    }

    @Test
    public void testMergeWith() throws InterruptedException {
        Flux<String> flux1 = Flux.just("a", "b").delayElements(Duration.ofMillis(200));
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> fluxMerged = flux1.mergeWith(flux2);

        // Obs: running in different threads
        fluxMerged.subscribe(log::info);

        Thread.sleep(1000);

        StepVerifier.create(fluxMerged)
                .expectSubscription()
                .expectNext("c","d","a","b")
                .verifyComplete();

    }

    @Test
    public void testMergeSequential() throws InterruptedException {
        Flux<String> flux1 = Flux.just("a", "b").delayElements(Duration.ofMillis(200));
        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> fluxMerged = Flux.mergeSequential(flux1, flux2, flux1);

        StepVerifier.create(fluxMerged)
                .expectSubscription()
                .expectNext("a","b","c","d","a","b")
                .verifyComplete();

    }

    @Test
    public void testMergeDelayError() {
        Flux<String> flux1 = Flux.just("a", "b")
                .map(s -> {
                    if("b".equals(s))
                        throw new IllegalArgumentException();
                    return s;
                }).doOnError(t->log.error("something bad happened"));

        Flux<String> flux2 = Flux.just("c", "d");

        Flux<String> fluxMerged = Flux.mergeDelayError(1, flux1, flux2);

        StepVerifier.create(fluxMerged)
                .expectSubscription()
                .expectNext("a","c","d")
                .expectError()
                .verify();

    }

    @Test
    public void testFlatMap() throws InterruptedException {
        Flux<String> flux1 = Flux.just("a", "b");

//        Flux<Flux<String>> fluxFlux = flux1.map(String::toUpperCase)
//                .map(this::getData)
//                .log();

        Flux<String> fluxFlux = flux1.map(String::toUpperCase)
                .flatMap(this::getData)
                .log();

        fluxFlux.subscribe(s->log.info(s.toString()));

        Thread.sleep(500);
    }

    @Test
    public void testFlatMapSequential() throws InterruptedException {
        Flux<String> flux1 = Flux.just("a", "b").delayElements(Duration.ofMillis(200));

        // data ordering is not guaranteed
        Flux<String> fluxFlux = flux1.map(String::toUpperCase)
                .flatMap(this::getData)
                .log();

        StepVerifier.create(fluxFlux)
                .expectSubscription()
                .expectNext("a1", "b1", "b2", "b3", "a2", "a3")
                .expectComplete()
                .verify();

        Flux<String> fluxSeq = flux1.map(String::toUpperCase)
                .flatMapSequential(this::getData)
                .log();

        StepVerifier.create(fluxSeq)
                .expectSubscription()
                .expectNext("a1", "a2", "a3","b1", "b2", "b3")
                .expectComplete()
                .verify();
    }

    private Flux<String> getData(String name) {
        return name.equals("A") ? Flux.just("a1", "a2", "a3").delayElements(Duration.ofMillis(100)) : Flux.just("b1", "b2", "b3");
    }

    @Data
    @AllArgsConstructor
    @EqualsAndHashCode
    @ToString
    class Anime {
        private String title;
        private String studio;
        private int episodes;
    }

    @Test
    public void testZip(){
        Flux<String> titlesFlux = Flux.just("goki", "bak");
        Flux<String> studioFlux = Flux.just("RED-BLUE", "TMS");
        Flux<Integer> episodesFlux = Flux.just(12,24);

        Flux<Anime> animeFlux = Flux.zip(titlesFlux, studioFlux, episodesFlux)
                .flatMap(tuple -> Flux.just(new Anime(tuple.getT1(), tuple.getT2(), tuple.getT3())));

        animeFlux.subscribe(anime -> log.info(anime.toString()));
    }

    @Test
    public void testZipWith(){
        Flux<String> titlesFlux = Flux.just("goki", "bak");
        Flux<String> studioFlux = Flux.just("RED-BLUE", "TMS");
        Flux<Integer> episodesFlux = Flux.just(12,24);

        Flux<Anime> animeFlux = Flux.zip(titlesFlux, studioFlux, episodesFlux)
                .flatMap(tuple -> Flux.just(new Anime(tuple.getT1(), tuple.getT2(), tuple.getT3())));

        titlesFlux.zipWith(episodesFlux)
                        .flatMap(tuple -> Flux.just(new Anime(tuple.getT1(), null, tuple.getT2())));

        animeFlux.subscribe(anime -> log.info(anime.toString()));
    }
}
