package com.example.J_Settlement.Ledger.model;

import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Block {
    private String hash;         // Mã băm của khối này
    private String previousHash; // Mã băm của khối trước đó (Liên kết chuỗi)
    private long timeStamp;      // Thời gian tạo khối
    private List<Transaction> transactions = new ArrayList<>();

    public Block(String previousHash) {
        this.previousHash = previousHash;
        this.timeStamp = System.currentTimeMillis();
    }
}
