package com.example.J_Settlement.Ledger.controller;

import com.example.J_Settlement.Ledger.model.BankNode;
import com.example.J_Settlement.Ledger.model.Block;
import com.example.J_Settlement.Ledger.model.Transaction;
import com.example.J_Settlement.Ledger.service.BlockchainService;
import com.example.J_Settlement.Ledger.until.CryptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/blockchain")
public class BlockchainController {

    @Autowired
    private BlockchainService blockchainService;

    // 1. Lấy toàn bộ chuỗi khối (Dùng để đối soát)
    @GetMapping("/ledger")
    public List<Block> getChain() {
        return blockchainService.getBlockchain();
    }

    // 2. Đăng ký một ngân hàng mới vào mạng lưới
    @PostMapping("/banks")
    public BankNode register(@RequestParam String name) {
        return blockchainService.registerBank(name);
    }

    // 3. Thực hiện chuyển khoản giữa 2 ngân hàng (Quyết toán)
    @PostMapping("/transfer")
    public String transfer(@RequestParam String from, @RequestParam String to, @RequestParam double amount) {
        BankNode sender = blockchainService.findBankByName(from);
        BankNode receiver = blockchainService.findBankByName(to);

        if (sender == null || receiver == null) return "Lỗi: Không tìm thấy ngân hàng!";

        // Tạo giao dịch
        Transaction t = new Transaction(sender.getPublicKey(), receiver.getPublicKey(), amount);

        // Ký giao dịch bằng Private Key của người gửi (Bảo mật thực tế)
        String dataToSign = CryptoUtil.getStringFromKey(sender.getPublicKey()) +
                CryptoUtil.getStringFromKey(receiver.getPublicKey()) + amount;
        byte[] sig = CryptoUtil.applyECDSASig(sender.getPrivateKey(), dataToSign);
        t.setSignature(sig);

        // Đưa vào Blockchain
        boolean success = blockchainService.addTransaction(t, sender);
        return success ? "Quyết toán thành công!" : "Quyết toán thất bại (Check số dư)!";
    }
}