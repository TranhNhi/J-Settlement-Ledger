package com.example.J_Settlement.Ledger.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Data
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender", nullable = false)
    private String sender;      // Public key của người gửi

    @Column(name = "recipient", nullable = false)
    private String recipient;   // Public key của người nhận

    @Column(name = "amount", nullable = false)
    private double amount;

    @Column(name = "timestamp", nullable = false)
    private long timestamp;

    @Column(name = "sender_bank", nullable = false)
    private String senderBank;  // Ngân hàng người gửi (VCB, BIDV...)

    @Column(name = "recipient_bank", nullable = false)
    private String recipientBank; // Ngân hàng người nhận

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "block_hash")
    private String blockHash;   // Null khi còn trong mempool, có giá trị sau khi đóng khối

    @Lob
    @Column(name = "signature", columnDefinition = "LONGBLOB", nullable = false)
    private byte[] signature;

    public enum TransactionStatus {
        PENDING, // đang chờ trong mempool
        CONFIRMED // đã đóng khối thành công
    }

    public Transaction(String sender, String recipient, double amount,
                       String senderBank, String recipientBank, byte[] signature) {
        this.sender = sender;
        this.recipient = recipient;
        this.amount = amount;
        this.senderBank = senderBank;
        this.recipientBank = recipientBank;
        this.signature = signature;
        this.timestamp = System.currentTimeMillis();
        this.status = TransactionStatus.PENDING;
       // this.status = TransactionStatus.CONFIRMED;
    }
}