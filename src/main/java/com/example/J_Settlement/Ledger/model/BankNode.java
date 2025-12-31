package com.example.J_Settlement.Ledger.model;

import lombok.Getter;
import java.security.*;

@Getter
public class BankNode {
    private String bankName;
    private PublicKey publicKey;   // Dùng làm "Số tài khoản" công khai
    private PrivateKey privateKey; // Dùng để "Ký tên" phê duyệt giao dịch (Bảo mật)

    public BankNode(String bankName) {
        this.bankName = bankName;
        try {
            // Tạo cặp khóa ECDSA (Chuẩn bảo mật cao trong Blockchain)
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            keyGen.initialize(256, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            this.privateKey = keyPair.getPrivate();
            this.publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
