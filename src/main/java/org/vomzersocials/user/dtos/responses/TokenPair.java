package org.vomzersocials.user.dtos.responses;

import lombok.*;

@Data
@AllArgsConstructor
public class TokenPair {
    private String accessToken;
    private String refreshToken;
}
