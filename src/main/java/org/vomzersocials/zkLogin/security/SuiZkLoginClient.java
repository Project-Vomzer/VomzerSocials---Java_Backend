package org.vomzersocials.zkLogin.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    /**
     * @param zkProof  base64-encoded proof from the client
     * @param publicKey  The public key used for verification
     * @return the verified Sui address (e.g. "0xabc123…") or null if proof invalid / error
     */
    public String verifyProof(String zkProof, String publicKey) {
        try {
            // 1️⃣ Build JSON-RPC payload including the public key for verification
            String payload = objectMapper.writeValueAsString(
                    objectMapper.createObjectNode()
                            .put("jsonrpc", "2.0")
                            .put("id", 1)
                            .put("method", "sui_verifyZkLoginProof")
                            .set("params", objectMapper.createArrayNode()
                                    .add(zkProof)  // Adding zkProof
                                    .add(publicKey) // Adding publicKey for verification
                            )
            );

            HttpEntity<String> req = new HttpEntity<>(payload, headers);

            // 2️⃣ Send the request
            ResponseEntity<String> resp = restTemplate.exchange(
                    suiRpcUrl,
                    HttpMethod.POST,
                    req,
                    String.class
            );

            if (resp.getStatusCode() != HttpStatus.OK) {
                log.warn("Sui RPC returned non-200: {}", resp.getStatusCode());
                return null;
            }

            // 3️⃣ Parse the JSON-RPC response
            JsonNode root = objectMapper.readTree(resp.getBody());
            JsonNode error = root.path("error");
            if (!error.isMissingNode()) {
                log.warn("Error in Sui RPC: code={}, message={}",
                        error.path("code").asText(),
                        error.path("message").asText());
                return null;
            }

            JsonNode result = root.path("result");
            if (result.isTextual()) {
                return result.asText();  // Returning the verified Sui address
            } else {
                log.warn("Unexpected result format from Sui RPC: {}", result);
                return null;
            }

        } catch (Exception e) {
            log.error("Failed to verify zkLogin proof", e);
            return null;
        }
    }
}
