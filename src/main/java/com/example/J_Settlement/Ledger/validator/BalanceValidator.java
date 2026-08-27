package com.example.J_Settlement.Ledger.validator;

import com.example.J_Settlement.Ledger.Repository.WalletRepository;
import com.example.J_Settlement.Ledger.exception.BusinessException;
import com.example.J_Settlement.Ledger.exception.InsufficientBalanceException;
import com.example.J_Settlement.Ledger.model.Wallet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BalanceValidator implements TransactionValidator {
    @Autowired
    private WalletRepository walletRepository;

    @Override
    public void validate(String from, String to, double amount, byte[] sig) {
        Wallet sender = walletRepository.findById(from)
                .orElseThrow(() -> new BusinessException("Ví gửi không tồn tại"));
        if (sender.getBalance() < amount) {
            throw new InsufficientBalanceException("Số dư khả dụng không đủ");
        }
    }
}