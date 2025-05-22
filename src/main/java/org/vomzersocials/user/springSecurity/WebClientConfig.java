package org.vomzersocials.user.springSecurity;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${sui.rpc.url:https://fullnode.mainnet.sui.io:443}")
    private String suiRpcUrl;

    @Value("${sui.zkproof.url:http://localhost:3000/api/generate-wallet}")
    private String zkProofUrl;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient suiWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(suiRpcUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean
    public WebClient zkProofWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .baseUrl(zkProofUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}