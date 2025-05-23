package org.vomzersocials.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api")
public class HealthController {
    @GetMapping("/health")
    public Mono<String> healthCheck() {
        return Mono.just("Backend is up");
    }
}
