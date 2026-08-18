package com.example.J_Settlement.Ledger.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "wallets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Wallet {

    @Id
    @Column(name = "public_key", length = 255, nullable = false)
    private String publicKey; // Địa chỉ ví, đồng thời là khóa công khai để verify chữ ký

    @Column(name = "bank_name", nullable = false)
    private String bankName; // Tên ngân hàng sở hữu ví (VCB, BIDV...)

    @Column(name = "balance", nullable = false)
    private double balance;

    @Column(name = "owner_username", nullable = false)
    private String ownerUsername; // Username của người sở hữu ví

    @Version
    @Column(name = "version", nullable = false)
    private long version = 0; // Optimistic Locking — chống race condition

    public Wallet(String publicKey, String bankName, double balance, String ownerUsername) {
        this.publicKey = publicKey;
        this.bankName = bankName;
        this.balance = balance;
        this.ownerUsername = ownerUsername;
    }
}