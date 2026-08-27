package com.example.J_Settlement.Ledger.validator;

public interface TransactionValidator {
    void validate(String from, String to, double amount, byte[] sig);
}
