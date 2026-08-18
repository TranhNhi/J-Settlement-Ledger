package com.example.J_Settlement.Ledger.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "roles", nullable = false)
    private String roles; // "ROLE_USER" hoặc "ROLE_ADMIN"

    @Column(name = "bank_name", nullable = false)
    private String bankName; // VCB, BIDV...

    @Column(name = "wallet_address", nullable = false)
    private String walletAddress; // public_key của ví liên kết
}