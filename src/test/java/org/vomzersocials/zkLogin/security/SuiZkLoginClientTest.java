package org.vomzersocials.zkLogin.security;//package org.vomzersocials.zkLogin.security;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.*;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.test.util.ReflectionTestUtils;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Mono;
//import reactor.test.StepVerifier;
//
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class SuiZkLoginClientTest {
//
//    @Mock WebClient.Builder webClientBuilder;
//    @Mock WebClient webClient;
//    @Mock WebClient.RequestBodyUriSpec uriSpec;
//    @Mock WebClient.RequestHeadersSpec<?> headersSpec;
//    @Mock WebClient.ResponseSpec responseSpec;
//
//    private ObjectMapper objectMapper;
//    private SuiZkLoginClient client;
//
//    @BeforeEach
//    public void setUp() {
//        objectMapper = new ObjectMapper();
//        client = new SuiZkLoginClient(objectMapper, webClientBuilder, 2);
//        // fake base URL
//        ReflectionTestUtils.setField(client, "suiRpcUrl", "http://fake-rpc");
//
//        when(webClientBuilder.baseUrl(anyString()))
//                .thenReturn(webClientBuilder);
//        when(webClientBuilder.defaultHeader(eq(HttpHeaders.CONTENT_TYPE), eq(new String[]{MediaType.APPLICATION_JSON_VALUE})))
//                .thenReturn(webClientBuilder);
//        when(webClientBuilder.build())
//                .thenReturn(webClient);
//        when(webClient.post())
//                .thenReturn(uriSpec);
//        doReturn(headersSpec)
//                .when(uriSpec)
//                .bodyValue(any());
//        when(headersSpec.retrieve())
//                .thenReturn(responseSpec);
//    }
//
//    @Test
//    public void verifyProof_successfulTextualResult_test() throws Exception {
//        ObjectNode rpc = objectMapper.createObjectNode();
//        rpc.put("jsonrpc", "2.0");
//        rpc.put("id", 1);
//        rpc.put("result", "0xabc123");
//
//        JsonNode jsonNode = rpc;
//
//        when(responseSpec.bodyToMono(JsonNode.class))
//                .thenReturn(Mono.just(jsonNode));
//
//        StepVerifier.create(client.verifyProof("proof", "pubkey"))
//                .expectNextMatches(res ->
//                        res.isSuccess() &&
//                                "0xabc123".equals(res.getAddress()) &&
//                                "Address verification successful".equals(res.getErrorMessage())
//                )
//                .verifyComplete();
//    }
//
//    @Test
//    public void verifyProof_successfulObjectResult_test() throws Exception {
//        ObjectNode rpc = objectMapper.createObjectNode();
//        rpc.put("jsonrpc", "2.0");
//        rpc.put("id", 1);
//        ObjectNode result = rpc.putObject("result");
//        result.put("address", "0xdeadbeef");
//
//        JsonNode jsonNode = rpc;
//
//        when(responseSpec.bodyToMono(JsonNode.class))
//                .thenReturn(Mono.just(jsonNode));
//
//        StepVerifier.create(client.verifyProof("proof", "pubkey"))
//                .expectNextMatches(res ->
//                        res.isSuccess() &&
//                                "0xdeadbeef".equals(res.getAddress())
//                )
//                .verifyComplete();
//    }
//
//    @Test
//    public void verifyProof_rpcErrorField_test() throws Exception {
//        ObjectNode rpc = objectMapper.createObjectNode();
//        rpc.put("jsonrpc", "2.0");
//        rpc.put("id", 1);
//        ObjectNode error = rpc.putObject("error");
//        error.put("message", "bad proof");
//
//        JsonNode jsonNode = rpc;
//
//        when(responseSpec.bodyToMono(JsonNode.class))
//                .thenReturn(Mono.just(jsonNode));
//
//        StepVerifier.create(client.verifyProof("proof", "pubkey"))
//                .expectNextMatches(res ->
//                        !res.isSuccess() &&
//                                res.getErrorMessage().contains("Sui error: bad proof") &&
//                                res.getAddress() == null
//                )
//                .verifyComplete();
//    }
//
//    @Test
//    public void verifyProof_unexpectedResultFormat_test() throws Exception {
//        ObjectNode rpc = objectMapper.createObjectNode();
//        rpc.put("jsonrpc", "2.0");
//        rpc.put("id", 1);
//        rpc.putPOJO("result", 12345);
//
//        JsonNode jsonNode = rpc;
//
//        when(responseSpec.bodyToMono(JsonNode.class))
//                .thenReturn(Mono.just(jsonNode));
//
//        StepVerifier.create(client.verifyProof("proof", "pubkey"))
//                .expectNextMatches(res ->
//                        !res.isSuccess() &&
//                                res.getErrorMessage().contains("Unexpected result format") &&
//                                res.getAddress() == null
//                )
//                .verifyComplete();
//    }
//
//    @Test
//    public void verifyProof_lowLevelException_test() {
//        when(responseSpec.bodyToMono(JsonNode.class))
//                .thenReturn(Mono.error(new RuntimeException("network down")));
//
//        StepVerifier.create(client.verifyProof("proof", "pubkey"))
//                .expectNextMatches(res ->
//                        !res.isSuccess() &&
//                                res.getErrorMessage().toLowerCase().contains("internal error") &&
//                                res.getAddress() == null
//                )
//                .verifyComplete();
//    }
//}
