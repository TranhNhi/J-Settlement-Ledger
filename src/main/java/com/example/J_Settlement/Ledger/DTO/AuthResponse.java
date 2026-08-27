package com.example.J_Settlement.Ledger.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String publicKey; // Dùng để Frontend gọi API lấy balance
    private String bankName;  // Dùng để Frontend biết đang ở VCB hay BIDV
    private String username;  // Hiển thị tên người dùng trên Dashboard
    private String role;      // Phân quyền (ADMIN/USER)

}