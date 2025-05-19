package org.vomzersocials.user.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
public class SuiConfig {

    @Bean
    public WebClient suiWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl("https://vomzersocialsnodejsintegration-production.up.railway.app/api/create-sui-address-off-chain") // Replace with actual node service URL
                .build();
    }

    @Bean
    public SuiAddressClient suiAddressClient(WebClient suiWebClient) {
        return new SuiAddressClient() {
            @Override
            public Mono<SuiAddressResponse> createSuiAddress() {
                return suiWebClient.post()
                        .uri("/createSuiAddress")
                        .retrieve()
                        .bodyToMono(SuiAddressResponse.class)
                        .onErrorResume(e -> {
                            // Log and return empty response to avoid %PARSER_ERROR
                            System.err.println("Failed to create Sui address: " + e.getMessage());
                            return Mono.just(new SuiAddressResponse("", ""));
                        });
            }
        };
    }

    public interface SuiAddressClient {
        Mono<SuiAddressResponse> createSuiAddress();
    }

    public record SuiAddressResponse(String walletAddress, String privateKey) {
    }
}