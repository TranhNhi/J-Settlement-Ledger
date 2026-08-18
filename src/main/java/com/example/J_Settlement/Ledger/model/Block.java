package com.example.J_Settlement.Ledger.model;

import com.example.J_Settlement.Ledger.until.CryptoUtil;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "blocks")
@Data
@NoArgsConstructor
public class Block {

    @Id
    @Column(name = "hash", nullable = false)
    private String hash;

    @Column(name = "block_index", nullable = false)
    private Long index;

    @Column(name = "previous_hash", nullable = false)
    private String previousHash;

    @Column(name = "timestamp", nullable = false)
    private long timestamp;

    @Column(name = "state_root")
    private String stateRoot;


    // Hibernate tự persist toàn bộ TX mới vào CENTRAL DB.
    // CascadeType.MERGE giữ lại để có thể update status TX đã tồn tại.
    // Khi sync sang local DB, transactions list được set rỗng trước khi save
    // nên cascade không ảnh hưởng — TX local được saveAll() riêng.
    @OneToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name = "block_hash")
    private List<Transaction> transactions;

    public Block(String hash, String previousHash, Long index,
                 List<Transaction> transactions, String stateRoot) {
        this.hash = hash;
        this.previousHash = previousHash;
        this.index = index;
        this.transactions = transactions;
        this.stateRoot = stateRoot;
        this.timestamp = System.currentTimeMillis();
    }

    // Tính lại hash để verify tính toàn vẹn — dùng trong IntegrityService
    public String calculateHash() {
        String txData = transactions != null
                ? transactions.stream()
                .map(tx -> tx.getSender() + tx.getRecipient() + tx.getAmount())
                .reduce("", String::concat)
                : "";

        return CryptoUtil.applySha256(index + previousHash + timestamp + txData);
    }
}
