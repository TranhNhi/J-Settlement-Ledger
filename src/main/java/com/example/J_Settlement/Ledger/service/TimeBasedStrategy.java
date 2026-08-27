package com.example.J_Settlement.Ledger.service;

import com.example.J_Settlement.Ledger.config.DBContextHolder;
import com.example.J_Settlement.Ledger.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/* Logic đóng khối sau mỗi 10 giây nếu mempool có giao dịch */
@Service
public class TimeBasedStrategy implements BlockStrategy {

    @Autowired
    private BlockchainService blockchainService;

    @Override
    @Scheduled(fixedRate = 10000)
    public void checkAndExecute() {
        for (String bank : blockchainService.getAllBanks()) {
            if (bank.equals("CENTRAL")) continue; // CENTRAL không có mempool riêng

            List<Transaction> pending = blockchainService.getPendingTransactions(bank);
            if (pending.isEmpty()) continue;

            System.out.println(">>> [TIME-BASED] Bank: " + bank
                    + " có " + pending.size() + " tx, tiến hành đóng khối...");
            try {
                // Set DB trước khi gọi createBlock — createBlock sẽ tự
                // chuyển DB qua từng bước bên trong bằng DBContextHolder.
                // Set ở đây chỉ để an toàn cho lần đọc mempool cuối.
                DBContextHolder.setCurrentDb(bank);
                blockchainService.createBlock(pending, bank);
            } catch (Exception e) {
                System.err.println("!!! [TIME-BASED ERROR] Bank: " + bank
                        + " | " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Luôn dọn context sau mỗi bank để thread sạch cho vòng lặp tiếp theo
                DBContextHolder.clear();
            }
        }
    }
}