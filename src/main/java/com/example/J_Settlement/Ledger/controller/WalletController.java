package com.example.J_Settlement.Ledger.controller;

import com.example.J_Settlement.Ledger.DTO.ApiResponse;
import com.example.J_Settlement.Ledger.Repository.TransactionRepository;
import com.example.J_Settlement.Ledger.Repository.UserRepository;
import com.example.J_Settlement.Ledger.Repository.WalletRepository;
import com.example.J_Settlement.Ledger.config.DBContextHolder;
import com.example.J_Settlement.Ledger.model.Transaction;
import com.example.J_Settlement.Ledger.model.User;
import com.example.J_Settlement.Ledger.model.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Value("${app.bank.list}")
    private String bankListConfig;

    // ----------------------------------------------------------------
    // Lấy thông tin ví theo địa chỉ — dùng trong FE hiển thị số dư
    // ----------------------------------------------------------------
    @GetMapping("/{address}")
    public ResponseEntity<ApiResponse<Wallet>> getWallet(@PathVariable String address) {
        return walletRepository.findById(address.trim())
                .map(w -> ResponseEntity.ok(ApiResponse.success("Tìm thấy ví", w)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ----------------------------------------------------------------
    // Lấy tất cả ví — dùng cho admin
    // ----------------------------------------------------------------
    @GetMapping
    public ResponseEntity<ApiResponse<List<Wallet>>> getAllWallets() {
        return ResponseEntity.ok(ApiResponse.success("OK", walletRepository.findAll()));
    }

    // ----------------------------------------------------------------
    // Lấy ví của người dùng đang đăng nhập
    // ----------------------------------------------------------------
    @GetMapping("/my-wallet")
    public ResponseEntity<ApiResponse<Wallet>> getMyWallet(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + username));

            Wallet wallet = walletRepository.findById(user.getWalletAddress())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ví: " + user.getWalletAddress()));

            return ResponseEntity.ok(ApiResponse.success("OK", wallet));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    // Validate ví nhận — FE gọi sau khi người dùng chọn ngân hàng và nhập địa chỉ
    // GET /api/wallets/check?address=xxx&bank=BIDV
    // ----------------------------------------------------------------
    @GetMapping("/check")
    public ResponseEntity<ApiResponse<Map<String, String>>> checkWallet(
            @RequestParam String address,
            @RequestParam String bank) {
        try {
            DBContextHolder.setCurrentDb(bank.toUpperCase());
            return walletRepository.findById(address.trim())
                    .map(w -> ResponseEntity.ok(ApiResponse.success("Ví hợp lệ",
                            Map.of("ownerUsername", w.getOwnerUsername(),
                                    "bank", w.getBankName()))))
                    .orElse(ResponseEntity.status(404)
                            .body(ApiResponse.error("Ví không tồn tại trong ngân hàng " + bank)));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }

    // ----------------------------------------------------------------
    // Nạp tiền vào ví
    // ----------------------------------------------------------------
    @PutMapping("/deposit")
    public ResponseEntity<ApiResponse<Wallet>> deposit(@RequestBody Map<String, Object> request) {
        try {
            String publicKey = (String) request.get("publicKey");
            if (publicKey == null) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Thiếu publicKey!"));
            }

            Double amount = Double.valueOf(request.get("amount").toString());
            if(amount < 0 ) {
                return ResponseEntity.badRequest().body(ApiResponse.error("Số tiền nạp vào phải là số dương"));
            }
            String bankName = DBContextHolder.getCurrentDb();

            Wallet wallet = walletRepository.findById(publicKey.trim())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy ví: " + publicKey));

            wallet.setBalance(wallet.getBalance() + amount);
            walletRepository.save(wallet);

            // Lưu lịch sử nạp tiền
            Transaction depositTx = new Transaction(
                    "SYSTEM_DEPOSIT",
                    publicKey.trim(),
                    amount,
                    "SYSTEM",
                    bankName,
                    new byte[0]
            );
            transactionRepository.save(depositTx);

            return ResponseEntity.ok(ApiResponse.success("Nạp tiền thành công", wallet));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(ApiResponse.error(e.getMessage()));
        }
    }
}