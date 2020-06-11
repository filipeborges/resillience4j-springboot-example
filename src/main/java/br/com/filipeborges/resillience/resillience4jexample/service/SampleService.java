package br.com.filipeborges.resillience.resillience4jexample.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
public class SampleService {

    public Mono<String> emitOkSampleStatus() {
        return Mono.just("Sample Status - OK")
                .delayElement(Duration.ofSeconds(5L));
    }

    public Mono<String> emitFailSampleStatus() {
        return Mono.just("Sample Status - Fail");
    }

}
