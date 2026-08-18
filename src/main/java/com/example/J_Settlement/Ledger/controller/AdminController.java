package com.example.J_Settlement.Ledger.controller;

import com.example.J_Settlement.Ledger.DTO.ApiResponse;
import com.example.J_Settlement.Ledger.Repository.BlockRepository;
import com.example.J_Settlement.Ledger.Repository.TransactionRepository;
import com.example.J_Settlement.Ledger.Repository.UserRepository;
import com.example.J_Settlement.Ledger.Repository.WalletRepository;
import com.example.J_Settlement.Ledger.config.DBContextHolder;
import com.example.J_Settlement.Ledger.model.Transaction;
import com.example.J_Settlement.Ledger.model.User;
import com.example.J_Settlement.Ledger.model.Wallet;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.security.Security;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private BlockRepository blockRepository;
    @Autowired private TransactionRepository transactionRepository;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // ----------------------------------------------------------------
    // Xem danh sách tất cả user + ví trong ngân hàng hiện tại
    // ----------------------------------------------------------------
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<User>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success("OK", userRepository.findAll()));
    }

    // ----------------------------------------------------------------
    // Tạo user mới + generate cặp key + tạo ví
    // FE generate key, gửi lên: username, password, publicKey
    // BE lưu user + ví, KHÔNG biết privateKey
    // ----------------------------------------------------------------
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<Map<String, String>>> createUser(
            @RequestBody Map<String, String> request) {
        try {
            String username   = request.get("username");
            String password   = request.get("password");
            String publicKey  = request.get("publicKey");
            String bankName   = DBContextHolder.getCurrentDb();

            if (username == null || password == null || publicKey == null) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Thiếu thông tin: username, password, publicKey"));
            }

            if (userRepository.findByUsername(username).isPresent()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Username đã tồn tại!"));
            }

            // Tạo ví trước
            Wallet wallet = new Wallet(publicKey, bankName, 0.0, username);
            walletRepository.save(wallet);

            // Tạo user
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setBankName(bankName);
            user.setRoles("ROLE_USER");
            user.setWalletAddress(publicKey);
            userRepository.save(user);

            return ResponseEntity.ok(ApiResponse.success(
                    "Tạo tài khoản thành công!", Map.of("walletAddress", publicKey)));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    // Xóa user + ví
    // ----------------------------------------------------------------
    @DeleteMapping("/users/{username}")
    public ResponseEntity<ApiResponse<String>> deleteUser(@PathVariable String username) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + username));

            // Xóa ví trước (FK)
            walletRepository.findById(user.getWalletAddress())
                    .ifPresent(walletRepository::delete);

            userRepository.delete(user);

            return ResponseEntity.ok(ApiResponse.success("Đã xóa tài khoản: " + username, null));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    // Nạp tiền vào ví cho khách
    // ----------------------------------------------------------------
    @PutMapping("/users/{username}/deposit")
    public ResponseEntity<ApiResponse<Wallet>> deposit(
            @PathVariable String username,
            @RequestBody Map<String, Object> request) {
        try {
            Double amount = Double.valueOf(request.get("amount").toString());
            String bankName = DBContextHolder.getCurrentDb();

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + username));

            Wallet wallet = walletRepository.findById(user.getWalletAddress())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ví!"));

            wallet.setBalance(wallet.getBalance() + amount);
            walletRepository.save(wallet);

            // Lưu lịch sử nạp tiền
            Transaction depositTx = new Transaction(
                    "SYSTEM_DEPOSIT",
                    user.getWalletAddress(),
                    amount,
                    "SYSTEM",
                    bankName,
                    new byte[0]
            );
            depositTx.setStatus(Transaction.TransactionStatus.CONFIRMED);
            transactionRepository.save(depositTx);

            return ResponseEntity.ok(ApiResponse.success("Nạp tiền thành công!", wallet));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }
    // ----------------------------------------------------------------
    // Thống kê nhanh cho dashboard admin
    // ----------------------------------------------------------------
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(ApiResponse.success("OK", Map.of(
                "totalUsers", userRepository.countByRoles("ROLE_USER"), // ← chỉ đếm ROLE_USER
                "totalBlocks", blockRepository.count(),
                "totalTransactions", transactionRepository.count()
        )));
    }
}
