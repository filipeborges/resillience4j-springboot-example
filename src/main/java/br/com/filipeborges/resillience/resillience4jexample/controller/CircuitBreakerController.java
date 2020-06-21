package br.com.filipeborges.resillience.resillience4jexample.controller;

import br.com.filipeborges.resillience.resillience4jexample.service.SampleService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/circuit-breaker")
public class CircuitBreakerController {
    final SampleService sampleService;
    final CircuitBreaker circuitBreaker;

    public CircuitBreakerController(SampleService sampleService) {
        this.sampleService = sampleService;
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(6)
                .failureRateThreshold(50)
                .permittedNumberOfCallsInHalfOpenState(2)
                .minimumNumberOfCalls(2)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .build();
        this.circuitBreaker = CircuitBreaker.of("CircuitBreakerSimple", config);
    }

    // This is a sample without spring boot annotations
    @GetMapping
    public Mono<ResponseEntity<String>> getStatus(@RequestParam("fail") Boolean mustFail) {
        return Mono.just(mustFail)
                .flatMap(shouldFail -> {
                    if (shouldFail) return sampleService.emitException();
                    return sampleService.emitOkSampleStatus();
                })
                .transform(CircuitBreakerOperator.of(this.circuitBreaker))
                .map(ResponseEntity::ok);
    }

    // This is the equivalent using spring boot annotations
    @GetMapping("/spring-boot")
    @io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker(name = "circuitBreakerAnnotation")
    public Mono<ResponseEntity<String>> getStatusSpringBoot(@RequestParam("fail") Boolean mustFail) {
        return Mono.just(mustFail)
                .flatMap(shouldFail -> {
                    if (shouldFail) return sampleService.emitException();
                    return sampleService.emitOkSampleStatus();
                })
                .map(ResponseEntity::ok);
    }
}
