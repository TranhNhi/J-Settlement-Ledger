package com.example.J_Settlement.Ledger.Repository;

import com.example.J_Settlement.Ledger.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // User: chỉ thấy tx của mình
    List<Transaction> findBySenderOrRecipientOrderByTimestampDesc(
            String sender, String recipient);

    // Admin: toàn bộ tx trong ngân hàng
    List<Transaction> findAllByOrderByTimestampDesc();

    // Lấy tx theo blockHash — dùng khi build BlockWithTx response cho Explorer
    List<Transaction> findByBlockHash(String blockHash);

    // Lấy tx liên quan đến một ngân hàng theo blockHash
    // Dùng khi sync local DB: chỉ lưu tx mà bank đó là sender HOẶC recipient
    @Query("SELECT t FROM Transaction t WHERE t.blockHash = :blockHash " +
            "AND (t.senderBank = :bank OR t.recipientBank = :bank)")
    List<Transaction> findByBlockHashAndBank(
            @Param("blockHash") String blockHash,
            @Param("bank") String bank);
}