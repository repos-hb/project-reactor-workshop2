package code;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

public class ThenMethodTest {

    @Test
    public void thenTest(){
        Mono.empty()
                .then()
                .doOnSuccess(i -> System.out.println("On success: " + i))
                .doOnError(i -> System.out.println("On error: " + i))
                .block();

        Mono.empty()
                .then(Mono.just("Good bye"))
                .doOnSuccess(i -> System.out.println("On success: " + i))
                .doOnError(i -> System.out.println("On error: " + i))
                .block();

        Mono.just("Hello World")
                .then(Mono.just("Good bye"))
                .doOnSuccess(i -> System.out.println("On success: " + i))
                .doOnError(i -> System.out.println("On error: " + i))
                .block();

//        Mono.error(new RuntimeException("Something wrong"))
//                .then(Mono.just("Good bye"))
//                .doOnSuccess(i -> System.out.println("On success: " + i))
//                .doOnError(i -> System.out.println("On error: " + i))
//                .block();

//        Mono.error(new RuntimeException("Something wrong"))
//                .then(Mono.error(new RuntimeException("Something very wrong")))
//                .doOnSuccess(i -> System.out.println("On success: " + i))
//                .doOnError(i -> System.out.println("On error: " + i))
//                .block();
    }
}
