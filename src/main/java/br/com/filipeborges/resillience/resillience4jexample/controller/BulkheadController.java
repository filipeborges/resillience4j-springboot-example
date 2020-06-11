package br.com.filipeborges.resillience.resillience4jexample.controller;

import br.com.filipeborges.resillience.resillience4jexample.service.SampleService;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
@RequestMapping("/bulkhead")
public class BulkheadController {

    final SampleService sampleService;
    final Bulkhead bulkhead;

    public BulkheadController(SampleService sampleService) {
        this.sampleService = sampleService;

        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(1)
                .maxWaitDuration(Duration.ZERO)
                .build();
        // This bulkhead could be injected, because it will be a singleton on Application Context
        this.bulkhead = Bulkhead.of("BulkheadSimple", config);
    }

    // This is a sample without spring boot annotations
    @GetMapping
    public Mono<ResponseEntity<String>> getStatus() {
        return sampleService
                .emitOkSampleStatus()
                .transformDeferred(BulkheadOperator.of(bulkhead))
                .onErrorResume(BulkheadFullException.class, e -> sampleService.emitFailSampleStatus())
                .map(ResponseEntity::ok);
    }

    // This is the equivalent using spring boot annotations
    @GetMapping("/spring-boot")
    @io.github.resilience4j.bulkhead.annotation.Bulkhead(name = "bulkheadAnnotation")
    public Mono<ResponseEntity<String>> getStatusSpringBoot() {
        return sampleService
                .emitOkSampleStatus()
                .map(ResponseEntity::ok);
    }

    @ExceptionHandler
    public Mono<ResponseEntity<String>> handleBulkheadAnnotationException(BulkheadFullException e) {
        return sampleService
                .emitFailSampleStatus()
                .map(ResponseEntity::ok);
    }
}
