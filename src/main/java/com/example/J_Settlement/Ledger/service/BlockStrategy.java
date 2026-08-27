package com.example.J_Settlement.Ledger.service;

public interface BlockStrategy {
    // Chiến lược chủ động kiểm tra và thực hiện đóng khối
    void checkAndExecute();
}