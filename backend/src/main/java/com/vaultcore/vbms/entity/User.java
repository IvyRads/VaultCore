package com.vaultcore.vbms.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name="users") @Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="user_id") private Integer userId;
    @Column(nullable=false, unique=true) private String email;
    @JsonIgnore @Column(name="password_hash", nullable=false) private String passwordHash;
    @Column(nullable=false) private String role;
    @Column(name="is_verified") private Boolean verified;
    @Column(name="created_at") private LocalDateTime createdAt;
    @Column(name="last_login") private LocalDateTime lastLogin;
}
