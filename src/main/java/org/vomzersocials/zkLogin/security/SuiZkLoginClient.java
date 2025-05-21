package org.vomzersocials.zkLogin.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class SuiZkLoginClient {

    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;
    private final MeterRegistry meterRegistry;

    @Value("${sui.rpc.url:https://fullnode.mainnet.sui.io:443}")
    private String suiRpcUrl;

    @Value("${sui.zkproof.url:http://localhost:3000/api/generate-wallet}")
    private String zkProofUrl;

    private WebClient suiWebClient;
    private WebClient zkProofWebClient;

    @PostConstruct
    public void init() {
        this.suiWebClient = webClientBuilder
                .baseUrl(suiRpcUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.zkProofWebClient = webClientBuilder
                .baseUrl(zkProofUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public Mono<VerifiedAddressResult> verifyProof(String zkProof, String publicKey) {
        return meterRegistry.timer("sui.zklogin.verifyProof.duration").record(() -> {
            ObjectNode rpcRequest = objectMapper.createObjectNode();
            rpcRequest.put("jsonrpc", "2.0");
            rpcRequest.put("id", 1);
            rpcRequest.put("method", "suix_verifyZkLoginProof");

            ArrayNode params = objectMapper.createArrayNode();
            params.add(zkProof);
            params.add(publicKey);
            rpcRequest.set("params", params);

            log.debug("Sending verifyProof request to {}: zkProofLength={}, publicKey={}",
                    suiRpcUrl, zkProof != null ? zkProof.length() : 0, publicKey);

            return suiWebClient.post()
                    .bodyValue(rpcRequest)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(5))
                    .map(resp -> {
                        JsonNode error = resp.path("error");
                        if (!error.isMissingNode()) {
                            String errorMsg = error.path("message").asText("Unknown error");
                            log.error("Sui RPC error: {}", errorMsg);
                            return VerifiedAddressResult.failed("Sui error: " + errorMsg);
                        }

                        JsonNode result = resp.path("result");
                        JsonNode addressNode = result.path("address");
                        if (addressNode.isTextual()) {
                            String address = addressNode.asText();
                            log.info("Verified zkProof: address={}", address);
                            return VerifiedAddressResult.success(address);
                        }
                        log.error("Unexpected result format: {}", result);
                        return VerifiedAddressResult.failed("Unexpected result format");
                    })
                    .onErrorResume(e -> {
                        log.error("ZK Proof verification failed for publicKey={}: {}", publicKey, e.getMessage(), e);
                        return Mono.just(VerifiedAddressResult.failed("Internal error: " + e.getMessage()));
                    });
        });
    }

    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
    public Mono<String[]> generateZkProofAndPublicKey(String jwt, String salt) {
        return meterRegistry.timer("sui.zklogin.generateZkProof.duration").record(() -> {
            ObjectNode request = objectMapper.createObjectNode();
            request.put("jwt", jwt);
            request.put("salt", salt);

            log.debug("Sending generateZkProof request to {}: salt={}", zkProofUrl, salt);

            return zkProofWebClient.post()
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .timeout(Duration.ofSeconds(10))
                    .map(resp -> {
                        JsonNode error = resp.path("error");
                        if (!error.isMissingNode()) {
                            String errorMsg = error.path("message").asText("Unknown error");
                            log.error("zkProof generation error: {}", errorMsg);
                            throw new IllegalArgumentException("zkProof generation failed: " + errorMsg);
                        }

                        JsonNode zkProofNode = resp.path("zkProof");
                        JsonNode publicKeyNode = resp.path("publicKey");
                        if (zkProofNode.isTextual() && publicKeyNode.isTextual()) {
                            String[] result = new String[]{zkProofNode.asText(), publicKeyNode.asText()};
                            log.info("Generated zkProof and publicKey: publicKey={}", result[1]);
                            return result;
                        }
                        log.error("Unexpected response format: {}", resp);
                        throw new IllegalArgumentException("Unexpected zkProof response format");
                    })
                    .onErrorResume(e -> {
                        log.error("zkProof generation failed: {}", e.getMessage(), e);
                        return Mono.error(new IllegalArgumentException("zkProof generation failed: " + e.getMessage(), e));
                    });
        });
    }
}