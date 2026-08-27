package com.example.J_Settlement.Ledger.service;

import com.example.J_Settlement.Ledger.Repository.BlockRepository;
import com.example.J_Settlement.Ledger.Repository.TransactionRepository;
import com.example.J_Settlement.Ledger.Repository.WalletRepository;
import com.example.J_Settlement.Ledger.model.Block;
import com.example.J_Settlement.Ledger.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BlockPersistenceService {

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Block getLastBlock() {
        return blockRepository.findTopByOrderByTimestampDesc();
    }

    // Lưu block + tx vào CENTRAL DB.
    // TX được saveAll riêng để id được CENTRAL DB assign tường minh.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveCentralBlock(Block block) {
        List<Transaction> txs = new ArrayList<>(block.getTransactions());
        block.setTransactions(new ArrayList<>());
        blockRepository.save(block);
        transactionRepository.saveAll(txs);

        if (!txs.isEmpty()) {
            Long minId = txs.stream().mapToLong(Transaction::getId).min().getAsLong();
            Long maxId = txs.stream().mapToLong(Transaction::getId).max().getAsLong();
            System.out.println(">>> [CENTRAL] Đã lưu " + txs.size()
                    + " TX. ID range: " + minId + " → " + maxId);
        }
    }

    // Sync block sang Local DB của một ngân hàng.
    // KEY FIX: TX đến từ CENTRAL đã có id — phải reset id = null
    // để Local DB tự sinh id riêng (INSERT), tránh StaleObjectStateException.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void upsertLocalBlock(String hash, String previousHash, Long index,
                                 String stateRoot, long timestamp,
                                 List<Transaction> allTxs, String bankName) {
        if (blockRepository.existsById(hash)) return;

        // Lọc TX liên quan đến ngân hàng này
        List<Transaction> bankTxs = allTxs.stream()
                .filter(tx -> bankName.equalsIgnoreCase(tx.getSenderBank())
                        || bankName.equalsIgnoreCase(tx.getRecipientBank()))
                .collect(Collectors.toList());

        // Lưu block rỗng trước
        Block localBlock = new Block();
        localBlock.setHash(hash);
        localBlock.setPreviousHash(previousHash);
        localBlock.setIndex(index);
        localBlock.setStateRoot(stateRoot);
        localBlock.setTimestamp(timestamp);
        localBlock.setTransactions(new ArrayList<>());
        blockRepository.save(localBlock);

        // Clone TX với id = null để Local DB tự sinh id riêng
        // Không dùng id từ CENTRAL vì đó là sequence của DB khác
        List<Transaction> localTxs = bankTxs.stream()
                .map(tx -> {
                    Transaction localTx = new Transaction(
                            tx.getSender(),
                            tx.getRecipient(),
                            tx.getAmount(),
                            tx.getSenderBank(),
                            tx.getRecipientBank(),
                            tx.getSignature()
                    );
                    localTx.setBlockHash(tx.getBlockHash());
                    localTx.setStatus(tx.getStatus());
                    localTx.setTimestamp(tx.getTimestamp());
                    // id = null → Hibernate sẽ INSERT, không MERGE
                    return localTx;
                })
                .collect(Collectors.toList());

        if (!localTxs.isEmpty()) {
            transactionRepository.saveAll(localTxs);
        }

        System.out.println(">>> [LOCAL:" + bankName + "] Block #" + index
                + " sync xong — " + bankTxs.size() + "/" + allTxs.size() + " tx liên quan");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deductFromSender(String senderAddr, double amount, String senderBank) {
        walletRepository.findByIdForUpdate(senderAddr).ifPresentOrElse(sender -> {
            sender.setBalance(sender.getBalance() - amount);
            walletRepository.save(sender);
            System.out.println("[-] Trừ " + amount + " từ ví: " + senderAddr
                    + " | DB: " + senderBank);
        }, () -> System.err.println("!!! Không tìm thấy ví gửi: " + senderAddr));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void addToRecipient(String recipientAddr, double amount, String recipientBank) {
        walletRepository.findByIdForUpdate(recipientAddr).ifPresentOrElse(recipient -> {
            recipient.setBalance(recipient.getBalance() + amount);
            walletRepository.save(recipient);
            System.out.println("[+] Cộng " + amount + " vào ví: " + recipientAddr
                    + " | DB: " + recipientBank);
        }, () -> System.err.println("!!! Không tìm thấy ví nhận: " + recipientAddr));
    }
}