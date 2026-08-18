package com.example.J_Settlement.Ledger.controller;

import com.example.J_Settlement.Ledger.Repository.UserRepository;
import com.example.J_Settlement.Ledger.config.DBContextHolder;
import com.example.J_Settlement.Ledger.config.JwtTokenProvider;
import com.example.J_Settlement.Ledger.DTO.LoginRequest;
import com.example.J_Settlement.Ledger.model.User;
import com.example.J_Settlement.Ledger.model.Wallet;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("http://localhost:5173")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private com.example.J_Settlement.Ledger.Repository.WalletRepository walletRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user,
                                          @RequestHeader("X-Bank-Name") String bankName) {
        // 1. Ép đúng Database dựa trên Header
        DBContextHolder.setCurrentDb(bankName.toUpperCase());

        if(userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username đã tồn tại!");
        }

        // 2. Mã hóa mật khẩu
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setBankName(bankName.toUpperCase());

        // --- SỬA TẠI ĐÂY: Tự động gán ROLE_USER nếu bị trống ---
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            user.setRoles("ROLE_USER");
        }

        // 3. Lưu xuống DB
        userRepository.save(user);

        return ResponseEntity.ok("Đăng ký thành công cho " + bankName);
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest,
                                              @RequestHeader("X-Bank-Name") String bankName) {
        try {
            // BƯỚC 1: ÉP DATABASE NGAY LẬP TỨC
            String routingKey = bankName.toUpperCase();
            DBContextHolder.setCurrentDb(routingKey);
            System.out.println("--- DEBUG LOGIN START ---");
            System.out.println(">>> 1. Target Bank: " + routingKey);

            // BƯỚC 2: XÁC THỰC NGƯỜI DÙNG
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            System.out.println(">>> 2. Authentication: SUCCESS");

            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy user: " + loginRequest.getUsername()));

            System.out.println(">>> 3. Found User: " + user.getUsername() + " in DB: " + routingKey);

            String jwt = tokenProvider.generateToken(user.getUsername(), user.getBankName());

            String publicKey = walletRepository.findByOwnerUsername(user.getUsername())
                    .map(Wallet::getPublicKey)
                    .orElse("");

            System.out.println(">>> 4. Final PublicKey Found: [" + publicKey + "]");
            if (publicKey.isEmpty()) {
                System.out.println("!!! CẢNH BÁO: Tìm thấy User nhưng bảng Wallets không có username này!");
            }

            // BƯỚC 6: ĐÓNG GÓI PHẢN HỒI
            Map<String, Object> response = new HashMap<>();
            response.put("accessToken", jwt);
            response.put("bankName", user.getBankName());
            response.put("publicKey", publicKey);
            response.put("username", user.getUsername());
            response.put("roles", user.getRoles());

            System.out.println("--- DEBUG LOGIN END ---");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("!!! LỖI LOGIN: " + e.getMessage());
            return ResponseEntity.status(401).body("Đăng nhập thất bại: " + e.getMessage());
        }
    }
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Với JWT đơn giản, chỉ cần trả về thông báo thành công
        // Frontend sẽ tự xóa Token ở phía người dùng.
        return ResponseEntity.ok("Đăng xuất thành công! Hẹn gặp lại Trang nhé.");
    }
}