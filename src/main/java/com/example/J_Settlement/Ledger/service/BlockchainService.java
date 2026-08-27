package com.example.J_Settlement.Ledger.service;

import com.example.J_Settlement.Ledger.config.DBContextHolder;
import com.example.J_Settlement.Ledger.model.Block;
import com.example.J_Settlement.Ledger.model.Transaction;
import com.example.J_Settlement.Ledger.until.CryptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BlockchainService {

    @Autowired
    private BlockPersistenceService blockPersistenceService;

    // Mempool theo từng ngân hàng — key = "VCB", "BIDV"...
    private static final Map<String, List<Transaction>> mempoolMap = new ConcurrentHashMap<>();

    @Value("${app.bank.list}")
    private String bankListConfig;

    public List<String> getAllBanks() {
        List<String> banks = new ArrayList<>();
        for (String b : bankListConfig.split(",")) {
            banks.add(b.trim().toUpperCase());
        }
        return banks;
    }

    public void addTx(Transaction tx) {
        String bank = tx.getSenderBank().toUpperCase();
        mempoolMap.computeIfAbsent(bank, k -> Collections.synchronizedList(new ArrayList<>()));
        mempoolMap.get(bank).add(tx);
        System.out.println(">>> [MEMPOOL:" + bank + "] Nhận giao dịch mới. Size: "
                + mempoolMap.get(bank).size());
    }

    public List<Transaction> getPendingTransactions(String bank) {
        List<Transaction> txs = mempoolMap.get(bank.toUpperCase());
        return txs != null ? new ArrayList<>(txs) : new ArrayList<>();
    }

    public int getMempoolSize(String bank) {
        List<Transaction> txs = mempoolMap.get(bank.toUpperCase());
        return txs != null ? txs.size() : 0;
    }

    // ----------------------------------------------------------------
    // ĐÓNG KHỐI
    // synchronized: chỉ 1 thread đóng khối tại 1 thời điểm,
    // tránh race condition giữa TimeBasedStrategy và FixedSizeStrategy.
    // try/finally: đảm bảo DBContextHolder luôn được clear dù có lỗi,
    // tránh context leak sang request/thread khác.
    // ----------------------------------------------------------------
    public synchronized void createBlock(List<Transaction> txs, String senderBank) {
        if (txs == null || txs.isEmpty()) return;

        System.out.println("=== [BLOCK] Bắt đầu đóng khối cho: " + senderBank
                + " | " + txs.size() + " tx");

        try {
            // ── Bước 1: Lấy last block từ CENTRAL ──────────────────────
            DBContextHolder.setCurrentDb("CENTRAL");
            Block lastBlock = blockPersistenceService.getLastBlock();
            String prevHash  = (lastBlock == null) ? "0" : lastBlock.getHash();
            long   nextIndex = (lastBlock != null) ? lastBlock.getIndex() + 1 : 1;

            // Tính hash
            String txData = txs.stream()
                    .map(tx -> tx.getSender() + tx.getRecipient() + tx.getAmount())
                    .reduce("", String::concat);
            String stateRoot = CryptoUtil.applySha256(txData);
            String blockHash = CryptoUtil.applySha256(prevHash + txData + stateRoot + nextIndex);
            long   blockTs   = System.currentTimeMillis();

            // Đánh dấu CONFIRMED + gán blockHash cho tất cả tx
            txs.forEach(tx -> {
                tx.setBlockHash(blockHash);
                tx.setStatus(Transaction.TransactionStatus.CONFIRMED);
            });

            // ── Bước 2: Lưu block + toàn bộ tx vào CENTRAL DB ──────────
            DBContextHolder.setCurrentDb("CENTRAL");
            Block centralBlock = new Block(blockHash, prevHash, nextIndex,
                    new ArrayList<>(txs), stateRoot);
            centralBlock.setTimestamp(blockTs);
            blockPersistenceService.saveCentralBlock(centralBlock);
            System.out.println(">>> [CENTRAL] Block #" + nextIndex + " đã lưu");

            // ── Bước 3: Cập nhật số dư tại Local DB của từng bên ────────
            for (Transaction tx : txs) {
                // Trừ tiền bên gửi
                DBContextHolder.setCurrentDb(tx.getSenderBank().toUpperCase());
                blockPersistenceService.deductFromSender(
                        tx.getSender().trim(), tx.getAmount(), tx.getSenderBank());

                // Cộng tiền bên nhận
                DBContextHolder.setCurrentDb(tx.getRecipientBank().toUpperCase());
                blockPersistenceService.addToRecipient(
                        tx.getRecipient().trim(), tx.getAmount(), tx.getRecipientBank());
            }

            // ── Bước 4: Sync block sang Local DB ngân hàng gửi ──────────
            DBContextHolder.setCurrentDb(senderBank.toUpperCase());
            blockPersistenceService.upsertLocalBlock(
                    blockHash, prevHash, nextIndex, stateRoot, blockTs, txs, senderBank);

            // ── Bước 5: Sync block sang Local DB các ngân hàng nhận ─────
            Set<String> recipientBanks = new HashSet<>();
            txs.forEach(tx -> recipientBanks.add(tx.getRecipientBank().toUpperCase()));
            recipientBanks.remove(senderBank.toUpperCase());

            for (String rBank : recipientBanks) {
                DBContextHolder.setCurrentDb(rBank);
                blockPersistenceService.upsertLocalBlock(
                        blockHash, prevHash, nextIndex, stateRoot, blockTs, txs, rBank);
            }

            // ── Bước 6: Dọn mempool ──────────────────────────────────────
            mempoolMap.get(senderBank.toUpperCase()).removeAll(txs);
            System.out.println("=== [SUCCESS] BLOCK #" + nextIndex + " | HASH: " + blockHash);

        } catch (Exception e) {
            System.err.println("!!! [BLOCK ERROR] Đóng khối thất bại cho bank: " + senderBank
                    + " | " + e.getMessage());
            e.printStackTrace();

            // QUAN TRỌNG: Dù lỗi vẫn phải clear mempool
            // Nếu không, TX sẽ bị retry mãi mỗi 10s → block mới nhưng fail lại
            // TX đã được lưu CENTRAL rồi nên không cần retry
            List<Transaction> pool = mempoolMap.get(senderBank.toUpperCase());
            if (pool != null) pool.removeAll(txs);
            System.out.println(">>> [MEMPOOL CLEARED after error] bank: " + senderBank);

            throw e;

        } finally {
            // Luôn clear context sau khi đóng khối xong hoặc lỗi
            // Tránh context leak sang thread/request tiếp theo
            DBContextHolder.clear();
        }
    }
}