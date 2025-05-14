package org.vomzersocials.user.cors;

import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class VomzerWebSocketHandler implements WebSocketHandler {
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        Flux<WebSocketMessage> inbound = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(session::textMessage);

        return session.send(inbound);
    }
}
