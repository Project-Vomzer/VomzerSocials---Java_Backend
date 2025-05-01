package org.vomzersocials.zkLogin.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuiZkLoginClientTest {

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestBodyUriSpec uriSpec;

    @Mock
    private WebClient.RequestBodySpec bodySpec;

    @Mock
    private WebClient.RequestHeadersSpec<?> headersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private SuiZkLoginClient client;

    @BeforeEach
    void setUp() {
        client = new SuiZkLoginClient(new ObjectMapper(), webClientBuilder);
        ReflectionTestUtils.setField(client, "suiRpcUrl", "http://fake-rpc");
//        client.init();

        when(webClientBuilder.baseUrl(anyString())).thenReturn(webClientBuilder);
        when(webClientBuilder.build()).thenReturn(webClient);

        when(webClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(bodySpec);

        doReturn(headersSpec)
                .when(bodySpec)
                .bodyValue(anyString());

        when(headersSpec.retrieve()).thenReturn(responseSpec);
    }

    @Test
    void verifyProof_successfulTextualResult() {
        String json = "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":\"0xabc123\"}";
        when(responseSpec.toEntity(String.class))
                .thenReturn(Mono.just(new ResponseEntity<>(json, HttpStatus.OK)));

        Mono<VerifiedAddressResult> resultMono = client.verifyProof("proof", "pubkey");

        // Use StepVerifier to subscribe and verify the results asynchronously
        StepVerifier.create(resultMono)
                .expectNextMatches(result -> {
                    assertTrue(result.isSuccess());
                    assertEquals("0xabc123", result.getAddress());
                    assertEquals("Address verification successful", result.getErrorMessage());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void verifyProof_successfulObjectResult() {
        String json = "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":{\"address\":\"0xdeadbeef\"}}";
        when(responseSpec.toEntity(String.class))
                .thenReturn(Mono.just(new ResponseEntity<>(json, HttpStatus.OK)));

        Mono<VerifiedAddressResult> resultMono = client.verifyProof("proof", "pubkey");

        StepVerifier.create(resultMono)
                .expectNextMatches(result -> {
                    assertTrue(result.isSuccess());
                    assertEquals("0xdeadbeef", result.getAddress());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void verifyProof_http500Failure() {
        when(responseSpec.toEntity(String.class))
                .thenReturn(Mono.just(new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR)));

        Mono<VerifiedAddressResult> resultMono = client.verifyProof("proof", "pubkey");

        StepVerifier.create(resultMono)
                .expectNextMatches(result -> {
                    assertFalse(result.isSuccess());
                    assertNull(result.getAddress());
                    assertTrue(result.getErrorMessage().contains("Sui RPC status: 500"));
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void verifyProof_rpcErrorField() {
        String json = "{\"jsonrpc\":\"2.0\",\"id\":1,\"error\":{\"message\":\"bad proof\"}}";
        when(responseSpec.toEntity(String.class))
                .thenReturn(Mono.just(new ResponseEntity<>(json, HttpStatus.OK)));

        Mono<VerifiedAddressResult> resultMono = client.verifyProof("proof", "pubkey");

        StepVerifier.create(resultMono)
                .expectNextMatches(result -> {
                    assertFalse(result.isSuccess());
                    assertNull(result.getAddress());
                    assertTrue(result.getErrorMessage().contains("Sui error: bad proof"));
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void verifyProof_unexpectedResultFormat() {
        String json = "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":12345}";
        when(responseSpec.toEntity(String.class))
                .thenReturn(Mono.just(new ResponseEntity<>(json, HttpStatus.OK)));

        Mono<VerifiedAddressResult> resultMono = client.verifyProof("proof", "pubkey");

        StepVerifier.create(resultMono)
                .expectNextMatches(result -> {
                    assertFalse(result.isSuccess());
                    assertNull(result.getAddress());
                    assertTrue(result.getErrorMessage().contains("Unexpected result format"));
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void verifyProof_lowLevelException() {
        when(responseSpec.toEntity(String.class))
                .thenReturn(Mono.error(new RuntimeException("network down")));

        Mono<VerifiedAddressResult> resultMono = client.verifyProof("proof", "pubkey");

        StepVerifier.create(resultMono)
                .expectNextMatches(result -> {
                    assertFalse(result.isSuccess());
                    assertNull(result.getAddress());
                    assertTrue(result.getErrorMessage().toLowerCase().contains("internal error"));
                    return true;
                })
                .verifyComplete();
    }
}
