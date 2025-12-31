package com.example.J_Settlement.Ledger.service;

import com.example.J_Settlement.Ledger.model.BankNode;
import com.example.J_Settlement.Ledger.model.Block;
import com.example.J_Settlement.Ledger.model.Transaction;
import com.example.J_Settlement.Ledger.until.CryptoUtil;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class BlockchainService {
    private List<Block> blockchain = new ArrayList<>();

    public BlockchainService() {
        // 1. Tạo khối khởi nguyên (Genesis Block)
        Block genesisBlock = new Block("0");
        genesisBlock.setHash(calculateHash(genesisBlock));
        blockchain.add(genesisBlock);
    }

    // Hàm tính toán mã băm cho một khối
    public String calculateHash(Block block) {
        String dataToHash = block.getPreviousHash() +
                block.getTimeStamp() +
                block.getTransactions().toString();
        return CryptoUtil.applySha256(dataToHash);
    }

    // 2. Logic kiểm tra số dư (Quan trọng trong tài chính)
    public double getBalance(java.security.PublicKey address) {
        double balance = 1000.0; // Giả sử mỗi ngân hàng mới có sẵn 1000$ làm vốn
        for (Block block : blockchain) {
            for (Transaction t : block.getTransactions()) {
                if (t.getRecipient().equals(address)) balance += t.getAmount();
                if (t.getSender().equals(address)) balance -= t.getAmount();
            }
        }
        return balance;
    }

    // 3. Xử lý giao dịch và tạo khối mới
    public boolean addTransaction(Transaction t, BankNode senderNode) {
        // Kiểm tra số dư trước khi chuyển
        if (getBalance(t.getSender()) < t.getAmount()) {
            System.out.println("Lỗi: Số dư không đủ!");
            return false;
        }

        // Tạo khối mới chứa giao dịch này
        Block lastBlock = blockchain.get(blockchain.size() - 1);
        Block newBlock = new Block(lastBlock.getHash());
        newBlock.getTransactions().add(t);
        newBlock.setHash(calculateHash(newBlock));

        blockchain.add(newBlock);
        return true;
    }

    public List<Block> getBlockchain() {
        return blockchain;
    }
    private List<BankNode> registeredBanks = new ArrayList<>();

    public BankNode registerBank(String name) {
        BankNode newNode = new BankNode(name);
        registeredBanks.add(newNode);
        return newNode;
    }

    public List<BankNode> getRegisteredBanks() {
        return registeredBanks;
    }

    public BankNode findBankByName(String name) {
        return registeredBanks.stream()
                .filter(b -> b.getBankName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }
}
