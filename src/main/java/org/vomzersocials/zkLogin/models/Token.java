package org.vomzersocials.zkLogin.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.time.LocalDateTime;

@Setter
@Getter
@ToString
@Entity
public class Token {
    @Id
    private String id;
    private String token;
    private String ownerEmail;
    private LocalDateTime timeCreated =  LocalDateTime.now();
}

