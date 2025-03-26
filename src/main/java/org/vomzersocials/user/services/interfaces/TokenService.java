package org.vomzersocials.user.services.interfaces;

import org.vomzersocials.user.data.models.Token;

public interface TokenService {
    String  createToken(String email);
    Token findByUserEmail(String email);
    void deleteToken(String id);

}
