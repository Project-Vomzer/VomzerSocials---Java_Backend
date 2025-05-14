package org.vomzersocials.zkLogin.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@Entity
@Table(name = "tokens")
public class Token {
    @Id
    private String id;
    private String token;
    private String userName;
    private LocalDateTime timeCreated =  LocalDateTime.now();
    private LocalDateTime expiresAt;
}

