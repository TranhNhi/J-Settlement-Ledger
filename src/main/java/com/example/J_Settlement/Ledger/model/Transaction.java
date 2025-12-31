package com.example.J_Settlement.Ledger.model;

import lombok.Getter;
import lombok.Setter;
import java.security.PublicKey;

@Getter
@Setter
public class Transaction {
    private PublicKey sender;    // Người gửi
    private PublicKey recipient; // Người nhận
    private double amount;       // Số tiền
    private byte[] signature;    // Chữ ký số (Chứng minh giao dịch là thật)

    public Transaction(PublicKey sender, PublicKey recipient, double amount) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
    }
}
