package org.vomzersocials.zkLogin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import org.vomzersocials.user.services.implementations.AuthenticationServiceImpl;
import org.vomzersocials.zkLogin.services.ZkLoginService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.util.AssertionErrors.assertNotNull;

@SpringBootTest
public class SuiZkLoginClientTest {

    @Mock
    private ZkLoginService zkLoginService;

    @InjectMocks
    private AuthenticationServiceImpl authService;

    @Mock
    RestTemplate restTemplate;

    ObjectMapper objectMapper = new ObjectMapper();
    SuiZkLoginClient client;

    @BeforeEach
    public void setUp() {
        client = new SuiZkLoginClient(restTemplate, objectMapper);
        ReflectionTestUtils.setField(client, "suiRpcUrl", "http://fake-rpc");
        client.init();
    }

    @Test
    public void verifyProof_successfulResponse_returnsAddress() throws Exception {
        String body = "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":\"0xabc123\"}";
        ResponseEntity<String> resp = new ResponseEntity<>(body, HttpStatus.OK);

        when(restTemplate.exchange(
                eq("http://fake-rpc"),
                eq(POST),
                any(org.springframework.http.HttpEntity.class),
                eq(String.class))
        ).thenReturn(resp);

        VerifiedAddressResult result = client.verifyProof("any-proof", "any-pubkey");

        assertNotNull("Result should not be null", result);
        assertTrue(result.isSuccess());
        assertEquals("0xabc123", result.getAddress());
        assertEquals("Address verification successful", result.getErrorMessage());
    }

    @Test
    public void verifyProof_errorResponse_returnsNull() {
        ResponseEntity<String> response = new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(any(), any(), any(), eq(String.class)))
                .thenReturn(response);

        VerifiedAddressResult result = client.verifyProof("zkProof", "publicKey");

        assertNotNull("Result should not be null", result);
        assertFalse(result.isSuccess());
        assertNull(result.getAddress());
    }
}
