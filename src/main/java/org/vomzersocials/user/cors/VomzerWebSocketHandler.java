package org.vomzersocials.user.cors;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.WebSocketMessage;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class VomzerWebSocketHandler implements WebSocketHandler {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public @NonNull Mono<Void> handle(@NonNull WebSocketSession session) {
        String userId = getUserIdFromQuery(session);

        sessions.put(userId, session);
        log.info("User {} connected", userId);

        Flux<WebSocketMessage> incoming = session.receive()
                .doOnNext(message -> {
                    log.info("Received message from {}: {}", userId, message.getPayloadAsText());
                    handleMessage(userId, message.getPayloadAsText());
                })
                .doOnError(error -> log.error("WebSocket error for user {}: {}", userId, error.getMessage()))
                .doFinally(signalType -> {
                    log.info("User {} disconnected", userId);
                    sessions.remove(userId);
                });
        return incoming.then();
    }

    private void handleMessage(String senderId, String message) {
        try {
            String to = extractRecipientFromJson(message);
            String text = extractTextFromJson(message);

            WebSocketSession receiverSession = sessions.get(to);
            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.send(Mono.just(receiverSession.textMessage(text))).subscribe();
            } else {
                log.warn("User {} not connected", to);
            }
        } catch (Exception e) {
            log.error("Failed to handle message from {}: {}", senderId, e.getMessage());
        }
    }

    private String getUserIdFromQuery(WebSocketSession session) {
        return session.getHandshakeInfo().getUri().getQuery()
                .replace("userId=", "");
    }

    private String extractRecipientFromJson(String json) {
        return json.split("\"to\":\"")[1].split("\"")[0];
    }

    private String extractTextFromJson(String json) {
        return json.split("\"text\":\"")[1].split("\"")[0];
    }
}
