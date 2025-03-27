package org.vomzersocials.zkLogin.security;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class SuiZkLoginClient {

    private static final String SUI_RPC_URL = "https://fullnode.mainnet.sui.io";

    public String verifyProof(String zkProof) {
        RestTemplate restTemplate = new RestTemplate();
        String payload = "{ \"method\": \"sui_verifyZkLoginProof\", \"params\": [\"" + zkProof + "\"] }";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(payload, headers);

        ResponseEntity<String> response = restTemplate.exchange(SUI_RPC_URL, HttpMethod.POST, requestEntity, String.class);

        if (response.getStatusCode() == HttpStatus.OK) {
            return parseSuiAddress(response.getBody()); // Extract the Sui address from the response
        }
        return null;
    }

    private String parseSuiAddress(String response) {
        // Extract user's Sui address from JSON response
        return "0x1234..."; // Example (modify for real response parsing)
    }
}

