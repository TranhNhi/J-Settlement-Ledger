
package com.example.J_Settlement.Ledger.controller;

import com.example.J_Settlement.Ledger.DTO.ApiResponse;
import com.example.J_Settlement.Ledger.Repository.TransactionRepository;
import com.example.J_Settlement.Ledger.Repository.UserRepository;
import com.example.J_Settlement.Ledger.model.Transaction;
import com.example.J_Settlement.Ledger.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// Toàn bộ luồng gửi giao dịch đi qua POST /api/settlement/send
// để đảm bảo TX không bị lưu DB trước khi được đóng khối.
@RestController
@RequestMapping("/api/transactions")
@CrossOrigin(origins = "http://localhost:5173")
public class TransactionController {

    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;

    // ----------------------------------------------------------------
    // USER: Lấy lịch sử giao dịch của ví mình
    // GET /api/transactions/my-history
    // ----------------------------------------------------------------
    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<List<Transaction>>> getMyHistory(
            Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + username));

            String walletAddress = user.getWalletAddress();
            List<Transaction> txs = transactionRepository
                    .findBySenderOrRecipientOrderByTimestampDesc(walletAddress, walletAddress);

            return ResponseEntity.ok(ApiResponse.success("OK", txs));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    // ADMIN: Lấy toàn bộ lịch sử trong ngân hàng
    // GET /api/transactions/all
    // ----------------------------------------------------------------
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Transaction>>> getAllHistory() {
        try {
            List<Transaction> txs = transactionRepository.findAllByOrderByTimestampDesc();
            return ResponseEntity.ok(ApiResponse.success("OK", txs));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }
}