package com.example.J_Settlement.Ledger.service;

import com.example.J_Settlement.Ledger.config.DBContextHolder;
import com.example.J_Settlement.Ledger.model.Transaction;
import com.example.J_Settlement.Ledger.validator.TransactionValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SettlementService {

    @Autowired private List<TransactionValidator> validators;
    @Autowired private FixedSizeStrategy fixedSizeStrategy;
    @Autowired private BlockchainService blockchainService;

    // SettlementService chỉ có nhiệm vụ: validate → mempool → trigger strategy.
    // Việc ghi DB hoàn toàn thuộc về BlockPersistenceService khi đóng khối.

    public String process(String fromAddress, String toAddress,
                          double amount, byte[] sig,
                          String senderBank, String recipientBank) throws Exception {

        if (toAddress == null)    throw new Exception("toAddress bị NULL!");
        if (senderBank == null)   throw new Exception("senderBank bị NULL!");
        if (recipientBank == null) throw new Exception("recipientBank bị NULL!");

        System.out.println(">>> [STEP 1] Validate giao dịch từ: " + fromAddress
                + " | " + senderBank + " → " + recipientBank);

        // Bước 1: Set DB context về Local DB của ngân hàng gửi
        // để validator đọc số dư đúng nguồn
        DBContextHolder.setCurrentDb(senderBank.toUpperCase());

        // Bước 2: Validate chữ ký + số dư — chỉ ĐỌC DB, không GHI
        for (TransactionValidator v : validators) {
            v.validate(fromAddress, toAddress, amount, sig);
        }

        System.out.println(">>> [STEP 2] Validate OK. Tạo Transaction object...");

        // Bước 3: Tạo TX object trong RAM — KHÔNG lưu DB
        // TX chỉ được ghi vào DB khi block được đóng (tại BlockPersistenceService)
        Transaction tx = new Transaction(
                fromAddress.trim(),
                toAddress.trim(),
                amount,
                senderBank.toUpperCase(),
                recipientBank.toUpperCase(),
                sig
        );

        // Bước 4: Đưa vào mempool (in-memory ConcurrentHashMap)
        blockchainService.addTx(tx);
        System.out.println(">>> [STEP 3] Đã vào mempool [" + senderBank + "]. Size: "
                + blockchainService.getMempoolSize(senderBank));

        // Bước 5: Trigger chiến lược đóng khối theo số lượng (≥ threshold)
        // TimeBasedStrategy tự chạy theo @Scheduled, không cần gọi ở đây
        fixedSizeStrategy.checkAndExecute();

        return "Giao dịch đã được gửi vào Mempool. Đang chờ đóng khối...";
    }
}