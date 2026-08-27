package com.example.J_Settlement.Ledger.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class DataSourceInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Lấy tên ngân hàng từ Postman gửi lên
        String bankName = request.getHeader("X-Bank-Name");

        if (bankName != null && !bankName.isEmpty()) {
            DBContextHolder.setCurrentDb(bankName.toUpperCase());
        } else {
            DBContextHolder.setCurrentDb("CENTRAL"); // Mặc định vào Central cho an toàn
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        DBContextHolder.clear();
    }
}