package com.example.J_Settlement.Ledger.controller;

import com.example.J_Settlement.Ledger.DTO.ApiResponse;
import com.example.J_Settlement.Ledger.DTO.TransactionRequest;
import com.example.J_Settlement.Ledger.service.BlockchainService;
import com.example.J_Settlement.Ledger.service.SettlementService;
import com.example.J_Settlement.Ledger.until.CryptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;

@RestController
@RequestMapping("/api/settlement")
public class SettlementController {

    @Autowired
    private SettlementService settlementService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> process(@RequestBody TransactionRequest request) {
        System.out.println(">>> [CONTROLLER] Giao dịch từ: " + request.getFromAddress()
                + " | " + request.getSenderBank() + " → " + request.getRecipientBank());
        try {
            byte[] sigBytes = CryptoUtil.hexToBytes(request.getSignature());

            String message = settlementService.process(
                    request.getFromAddress(),
                    request.getToAddress(),
                    request.getAmount(),
                    sigBytes,
                    request.getSenderBank(),
                    request.getRecipientBank()
            );

            return ResponseEntity.ok(ApiResponse.success(message, null));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}