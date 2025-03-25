package org.vomzersocials.services.interfaces;

import org.vomzersocials.data.models.Token;

public interface TokenService {

    String  createToken(String email);
    Token findByUserEmail(String email);
    void deleteToken(String id);

}
