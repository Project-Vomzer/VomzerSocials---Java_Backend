package org.vomzersocials.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessage {
    private String senderId;
    private String message;
    private String timestamp;
}
