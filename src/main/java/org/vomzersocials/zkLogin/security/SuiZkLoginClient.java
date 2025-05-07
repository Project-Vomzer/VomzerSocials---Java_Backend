package org.vomzersocials.zkLogin.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class SuiZkLoginClient {

    private final ObjectMapper objectMapper;
    private final WebClient.Builder webClientBuilder;

    @Value("${sui.rpc.url:https://fullnode.mainnet.sui.io:443}")
    private String suiRpcUrl;

    public Mono<VerifiedAddressResult> verifyProof(String zkProof, String publicKey) {
        ObjectNode rpcRequest = objectMapper.createObjectNode();
        rpcRequest.put("jsonrpc", "2.0");
        rpcRequest.put("id", 1);
        rpcRequest.put("method", "suix_verifyZkLoginProof");

        ArrayNode params = objectMapper.createArrayNode();
        params.add(zkProof);
        params.add(publicKey);
        rpcRequest.set("params", params);

        WebClient webClient = webClientBuilder
                .baseUrl(suiRpcUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        return webClient.post()
                .bodyValue(rpcRequest)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(resp -> {
                    JsonNode error = resp.path("error");
                    if (!error.isMissingNode()) {
                        return VerifiedAddressResult.failed("Sui error: " + error.path("message").asText());
                    }

                    JsonNode result = resp.path("result");
                    JsonNode addressNode = result.get("address");
                    if (addressNode != null && addressNode.isTextual()) {
                        return VerifiedAddressResult.success(addressNode.asText());
                    } else if (result.isTextual()) {
                        return VerifiedAddressResult.success(result.asText());
                    } else {
                        return VerifiedAddressResult.failed("Unexpected result format");
                    }
                })
                .onErrorResume(e -> {
                    log.error("ZK Proof verification failed", e);
                    return Mono.just(VerifiedAddressResult.failed("Internal error"));
                });
    }
}
