package code;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class FluxTest {

    @Test
    public void testFluxCreationEmpty(){
        Flux<String> emp = Flux.empty();
        emp.log();

        emp.subscribe(log::info);
    }

    @Test
    public void testFluxCreationString(){
        Flux<String> flux = Flux.just("William", "Suane", "DevDojo", "Academy")
                .log();

        flux.subscribe(log::info,
                err -> err.printStackTrace(),   // error consumer
                () -> log.info("FINISH!!"),     // onComplete consumer
                subscription -> subscription.request(3));
    }

    @Test
    public void testFluxCreationInteger(){
        Flux<Integer> emp = Flux.range(1,5).log();
        emp.subscribe(integer -> log.info(String.valueOf(integer)));
    }

    @Test
    public void testFluxCreationList(){
        List<Integer> list = Arrays.asList(1,2,3,4,5);

        // method: fromIterable(Iterable)
        Flux<Integer> emp = Flux.fromIterable(list);

        // method: fromStream(Stream)
        Flux<Integer> flux = Flux.fromStream(list.stream());

        // method: fromStream(Stream)
        Flux<Integer> flux1 = Flux.fromStream(()->list.stream());
        flux1.subscribe(integer -> log.info(String.valueOf(integer)));
    }

    @Test
    public void testFluxCreationError(){
        Flux<Integer> emp = Flux.range(1,5)
                .log()
                .map(i-> {
                    if(i==4)
                        throw new IllegalArgumentException("4 not allowed");
                    return i;
                });

        emp.subscribe(integer -> log.info(String.valueOf(integer)),
                e->log.error("error happened"),
                ()->log.info("DONE"));
    }

    @Test
    public void testFluxBackpressure_uglyWay(){
        Flux<Integer> flux = Flux.range(1, 10).log();

        flux.subscribe(new Subscriber<Integer>() {

            private int requestCount=2;
            private int count=0;
            private Subscription subscription;

            @Override
            public void onSubscribe(Subscription s) {
                this.subscription=s;
                subscription.request(requestCount);
            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if(count==requestCount){
                    count=0;
                    this.subscription.request(requestCount);
                }
            }

            @Override
            public void onError(Throwable t) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Test
    public void testFluxBackpressure_notSoUglyWay(){
        Flux<Integer> flux = Flux.range(1,10).log();

        flux.subscribe(new BaseSubscriber<Integer>() {

            private int count=0;
            private int reqCount=2;
            private Subscription subscription;
            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                this.subscription=subscription;
                this.subscription.request(reqCount);
            }

            @Override
            protected void hookOnNext(Integer value) {
                count++;
                if(count==reqCount){
                    count=0;
                    subscription.request(reqCount);
                }
            }
        });
    }

    @Test
    public void testBackpressureWithRateLimit(){
        Flux<Integer> flux = Flux.range(1, 10)
                .log()
                .limitRate(3);

        flux.subscribe(i->log.info("Number {}",i));
    }

    @Test
    public void testFluxInterval() throws InterruptedException {
        Flux<Long> flux = Flux.interval(Duration.ofMillis(100))
                .take(10)
                .log();

        flux.subscribe(i->log.info("Number {}",i));

        // interval Flux emission is handled by another thread
        // main thread dies immediately if not put to sleep()
        Thread.sleep(3000);
    }

    @Test
    public void testFluxInterval2() throws InterruptedException {
        // emits after an initial delay of 1s and then at regular interval of 2s
        Flux<Long> flux = Flux.interval(Duration.ofMillis(1000),Duration.ofMillis(2000))
                .log();

        flux.subscribe(i->log.info("Number {}",i));

        Thread.sleep(9000);
    }

    @Test
    public void testFluxInterval3() throws InterruptedException {
          StepVerifier.withVirtualTime(this::createInterval)
                  .expectSubscription()
                  .thenAwait(Duration.ofDays(1L))
                  .expectNext(0L)
                  .thenCancel()
                  .verify();
    }

    @Test
    public void testFluxInterval4() throws InterruptedException {
        StepVerifier.withVirtualTime(this::createInterval)
                .expectSubscription()
                .expectNoEvent(Duration.ofHours(24))
                .expectNext(0L)
                .thenAwait(Duration.ofDays(1L))
                .expectNext(1L)
                .thenCancel()
                .verify();
    }

    private Flux<Long> createInterval() {
        return Flux.interval(Duration.ofDays(1L))
                .log();
    }

    @Test
    public void testConnectableFlux() throws InterruptedException {
        ConnectableFlux<Integer> connectableFlux = Flux.range(1, 10)
//                .log()
                .delayElements(Duration.ofMillis(200))
                .publish();

//        connectableFlux.connect();
//
//        log.info("Putting main thread to sleep for 300ms");
//        Thread.sleep(200);
//
//        connectableFlux.subscribe(i->log.info("Consumed ###{}",i));
//
//        log.info("Putting main thread to sleep for 200ms");
//        Thread.sleep(300);
//
//        connectableFlux.subscribe(i->log.info("Consumed ######{}",i));

        StepVerifier.create(connectableFlux)
                .then(connectableFlux::connect)
                .thenConsumeWhile(integer -> integer<=5)
                .expectNext(6,7,8,9,10)
                .expectComplete()
                .verify();
    }

    @Test
    public void testAutoConnectableFlux() throws InterruptedException {
        Flux<Integer> fluxAuto = Flux.range(1, 10)
                .delayElements(Duration.ofMillis(200))
                .publish()
                .autoConnect(2);

        StepVerifier.create(fluxAuto)
                .then(fluxAuto::subscribe)
                .thenConsumeWhile(integer -> integer<=5)
                .expectNext(6,7,8,9,10)
                .expectComplete()
                .verify();
    }

}
