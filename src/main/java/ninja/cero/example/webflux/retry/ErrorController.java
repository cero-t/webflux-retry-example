package ninja.cero.example.webflux.retry;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
public class ErrorController {
    @PostMapping("/error")
    ResponseEntity<String> error() {
        System.out.println("/error " + LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    int counter = 0;

    @PostMapping("/error_twice")
    ResponseEntity<String> errorTwice() {
        counter++;
        System.out.println(counter + " /error_twice " + LocalDateTime.now());
        if (counter % 3 == 0) {
            return ResponseEntity.ok("OK");
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @PostMapping("/error_twice_await")
    ResponseEntity<String> errorTwiceAwait() throws InterruptedException {
        Thread.sleep(2000L);

        counter++;
        System.out.println(counter + " /error_twice_await " + LocalDateTime.now());
        if (counter % 3 == 0) {
            return ResponseEntity.ok("OK");
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }

    @PostMapping("/error_503_404")
    ResponseEntity<String> errorTwice503() {
        counter++;
        System.out.println(counter + " /error_503_404 " + LocalDateTime.now());
        if (counter % 3 == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
    }
}
