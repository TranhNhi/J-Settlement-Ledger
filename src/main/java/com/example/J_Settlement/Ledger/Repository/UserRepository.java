package com.example.J_Settlement.Ledger.Repository;

import com.example.J_Settlement.Ledger.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Hàm này cực kỳ quan trọng để Spring Security tìm User lúc Đăng nhập
    Optional<User> findByUsername(String username);

    // Kiểm tra xem Username đã tồn tại chưa (dùng cho lúc Đăng ký)
    Boolean existsByUsername(String username);
    long countByRoles(String roles);
    long countByRolesAndBankName(String roles, String bankName);
}