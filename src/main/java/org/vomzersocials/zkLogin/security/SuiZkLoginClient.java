package org.vomzersocials.zkLogin.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
@RequiredArgsConstructor
public class SuiZkLoginClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${sui.rpc.url:https://fullnode.mainnet.sui.io:443}")
    private String suiRpcUrl;

    private HttpHeaders headers;

    @PostConstruct
    public void init() {
        headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

//        public VerifiedAddressResult verifyProof(String zkProof, String publicKey) {
//            try {
//                // build JSON–RPC payload …
//                ResponseEntity<String> resp = restTemplate.exchange(zkProof, HttpMethod.POST, new HttpEntity<>(publicKey, headers), String.class);
//
//                if (resp.getStatusCode() != HttpStatus.OK) {
//                    return VerifiedAddressResult.failed("Sui RPC status: " + resp.getStatusCode());
//                }
//
//                JsonNode root = objectMapper.readTree(resp.getBody());
//                JsonNode error = root.path("error");
//                if (!error.isMissingNode()) {
//                    return VerifiedAddressResult.failed("Sui error: " + error.path("message").asText());
//                }
//
//                JsonNode result = root.path("result");
//                if (result.isTextual()) {
//                    return VerifiedAddressResult.success(result.asText());
//                } else {
//                    return VerifiedAddressResult.failed("Bad result format");
//                }
//            } catch (Exception e) {
//                log.error("Proof verification failed", e);
//                return VerifiedAddressResult.failed("Internal error");
//            }
//        }

    public VerifiedAddressResult verifyProof(String zkProof, String publicKey) {
        try {
            ObjectNode rpcRequest = objectMapper.createObjectNode();
            rpcRequest.put("jsonrpc", "2.0");
            rpcRequest.put("id", 1);
            rpcRequest.put("method", "suix_verifyZkLoginProof");

            ArrayNode params = objectMapper.createArrayNode();
            params.add(zkProof);
            params.add(publicKey);
            rpcRequest.set("params", params);

            String requestBody = objectMapper.writeValueAsString(rpcRequest);
            ResponseEntity<String> resp = configureRPCUrl(requestBody);
            if (resp.getStatusCode() != HttpStatus.OK) {
                return VerifiedAddressResult.failed("Sui RPC status: " + resp.getStatusCode());
            }

            JsonNode root = objectMapper.readTree(resp.getBody());
            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                return VerifiedAddressResult.failed("Sui error: " + error.path("message").asText());
            }

            JsonNode result = root.path("result");
            if (result.isTextual()) {
                return VerifiedAddressResult.success(result.asText());
            } else {
                return VerifiedAddressResult.failed("Bad result format");
            }
        } catch (Exception e) {
            log.error("Proof verification failed", e);
            return VerifiedAddressResult.failed("Internal error");
        }
    }

    private ResponseEntity<String> configureRPCUrl(String requestBody) {
        HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
        return restTemplate.exchange(
                suiRpcUrl,
                HttpMethod.POST,
                entity,
                String.class
        );
    }


}


