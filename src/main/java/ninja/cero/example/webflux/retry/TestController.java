package ninja.cero.example.webflux.retry;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.time.LocalDateTime;

@RestController
public class TestController {
    WebClient webClient;

    public TestController(WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * エラーのリトライオーバー
     * @return RetryExhaustedExceptionがスローされる
     */
    @GetMapping("/test1")
    Mono<ResponseEntity<String>> test1() {
        return webClient.post()
                .uri("http://localhost:8080/error")
                .retrieve()
                .toEntity(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)));
    }

    /**
     * 2回エラーが起きた後に成功
     * @return "OK" が返る
     */
    @GetMapping("/test2")
    Mono<ResponseEntity<String>> test2() {
        return webClient.post()
                .uri("http://localhost:8080/error_twice")
                .retrieve()
                .toEntity(String.class)
                .doOnSuccess(r -> System.out.println(Thread.currentThread() + " Success " + LocalDateTime.now() + " " + r))
                .doOnError(th -> System.out.println(Thread.currentThread() + " Error " + LocalDateTime.now() + " " + th))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)));
    }

    /**
     * 2回エラーが起きた後に成功。ただしサーバ側で2秒ずつ待つ
     * @return "OK" が返る
     */
    @GetMapping("/test3")
    Mono<ResponseEntity<String>> test3() {
        return webClient.post()
                .uri("http://localhost:8080/error_twice_await")
                .retrieve()
                .toEntity(String.class)
                .doOnSuccess(r -> System.out.println(Thread.currentThread() + " Success " + LocalDateTime.now() + " " + r))
                .doOnError(th -> System.out.println(Thread.currentThread() + " Error " + LocalDateTime.now() + " " + th))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)));
    }

    /**
     * 最終的な失敗の時に処理を入れる
     * @return RetryExhaustedExceptionがスローされる
     */
    @GetMapping("/test4")
    Mono<ResponseEntity<String>> test4() {
        return webClient.post()
                .uri("http://localhost:8080/error")
                .retrieve()
                .toEntity(String.class)
                .doOnError(th -> System.out.println(Thread.currentThread() + " Error1 " + LocalDateTime.now() + " " + th))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)))
                .doOnError(th -> System.out.println(Thread.currentThread() + " Error2 " + LocalDateTime.now() + " " + th));
    }

    /**
     * 最終的な失敗をハンドリングする
     * @return 503エラーとメッセージの入ったレスポンス
     */
    @GetMapping("/test5")
    Mono<ResponseEntity<String>> test5() {
        return webClient.post()
                .uri("http://localhost:8080/error")
                .retrieve()
                .toEntity(String.class)
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)))
                .onErrorReturn(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body("リトライオーバー"));
    }

    /**
     * 2回503エラーが起きた後に404エラー
     * @return WebClientResponseException$NotFoundがスローされる
     */
    @GetMapping("/test6")
    Mono<ResponseEntity<String>> test6() {
        return webClient.post()
                .uri("http://localhost:8080/error_503_404")
                .retrieve()
                .toEntity(String.class)
                .doOnError(th -> System.out.println(Thread.currentThread() + " Error " + LocalDateTime.now() + " " + th))
                .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1))
                        .filter(th -> {
                            if (th instanceof WebClientResponseException ex) {
                                return ex.getStatusCode().value() == 503;
                            }
                            return false;
                        }));
    }
}
