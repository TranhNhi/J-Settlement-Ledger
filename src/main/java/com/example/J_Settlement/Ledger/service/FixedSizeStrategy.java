package com.example.J_Settlement.Ledger.service;

import com.example.J_Settlement.Ledger.config.DBContextHolder;
import com.example.J_Settlement.Ledger.model.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/* Logic đóng khối khi mempool đủ 10 giao dịch */
@Service
public class FixedSizeStrategy implements BlockStrategy {

    @Autowired
    private BlockchainService blockchainService;

    private static final int BLOCK_SIZE_THRESHOLD = 10;

    @Override
    public void checkAndExecute() {
        for (String bank : blockchainService.getAllBanks()) {
            if (bank.equals("CENTRAL")) continue;

            if (blockchainService.getMempoolSize(bank) >= BLOCK_SIZE_THRESHOLD) {
                List<Transaction> pending = blockchainService.getPendingTransactions(bank);
                System.out.println(">>> [FIXED-SIZE] Bank: " + bank
                        + " đủ " + BLOCK_SIZE_THRESHOLD + " tx, tiến hành đóng khối...");
                try {
                    DBContextHolder.setCurrentDb(bank);
                    blockchainService.createBlock(pending, bank);
                } catch (Exception e) {
                    System.err.println("!!! [FIXED-SIZE ERROR] Bank: " + bank
                            + " | " + e.getMessage());
                    e.printStackTrace();
                } finally {
                    DBContextHolder.clear();
                }
            }
        }
    }
}
