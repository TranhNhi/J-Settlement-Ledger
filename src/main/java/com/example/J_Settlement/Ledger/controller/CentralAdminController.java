package com.example.J_Settlement.Ledger.controller;

import com.example.J_Settlement.Ledger.DTO.ApiResponse;
import com.example.J_Settlement.Ledger.Repository.BlockRepository;
import com.example.J_Settlement.Ledger.Repository.TransactionRepository;
import com.example.J_Settlement.Ledger.Repository.UserRepository;
import com.example.J_Settlement.Ledger.Repository.WalletRepository;
import com.example.J_Settlement.Ledger.config.DBContextHolder;
import com.example.J_Settlement.Ledger.model.Block;
import com.example.J_Settlement.Ledger.model.Transaction;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/central")
public class CentralAdminController {

    @Autowired private BlockRepository blockRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private WalletRepository walletRepository;

    @Value("${app.bank.list}")
    private String bankListConfig;

    private List<String> getBanks() {
        return Arrays.stream(bankListConfig.split(","))
                .map(String::trim).map(String::toUpperCase).toList();
    }

    // ----------------------------------------------------------------
    // Tổng quan toàn hệ thống — đọc từ CENTRAL DB
    // ----------------------------------------------------------------
    @GetMapping("/overview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOverview() {
        DBContextHolder.setCurrentDb("CENTRAL");
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalBlocks", blockRepository.count());
        data.put("totalTransactions", transactionRepository.count());

        // Thống kê theo từng ngân hàng
        List<Map<String, Object>> bankStats = new ArrayList<>();
        for (String bank : getBanks()) {
            if (bank.equals("CENTRAL")) continue;
            DBContextHolder.setCurrentDb(bank);
            Map<String, Object> stat = new LinkedHashMap<>();
            stat.put("bank", bank);
            stat.put("totalUsers", userRepository.countByRolesAndBankName("ROLE_USER", bank));
            stat.put("totalWallets", walletRepository.count());
            stat.put("totalTransactions", transactionRepository.count());
            bankStats.add(stat);
        }
        data.put("bankStats", bankStats);

        return ResponseEntity.ok(ApiResponse.success("OK", data));
    }

    // ----------------------------------------------------------------
    // Xem tất cả blocks từ CENTRAL DB
    // ----------------------------------------------------------------
    @GetMapping("/blocks")
    public ResponseEntity<ApiResponse<List<Block>>> getAllBlocks() {
        DBContextHolder.setCurrentDb("CENTRAL");
        return ResponseEntity.ok(ApiResponse.success("OK",
                blockRepository.findAll()));
    }

    // ----------------------------------------------------------------
    // Xem tất cả transactions từ CENTRAL DB
    // ----------------------------------------------------------------
    @GetMapping("/transactions")
    public ResponseEntity<ApiResponse<List<Transaction>>> getAllTransactions() {
        DBContextHolder.setCurrentDb("CENTRAL");
        return ResponseEntity.ok(ApiResponse.success("OK",
                transactionRepository.findAll()));
    }
}