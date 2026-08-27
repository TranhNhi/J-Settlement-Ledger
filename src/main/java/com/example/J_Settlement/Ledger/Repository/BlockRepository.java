package com.example.J_Settlement.Ledger.Repository;

import com.example.J_Settlement.Ledger.model.Block;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockRepository extends JpaRepository<Block, String> {

    // Tìm khối được tạo ra gần đây nhất dựa trên thời gian
    // Hàm này được BlockchainService sử dụng để lấy previousHash

    Block findTopByOrderByTimestampDesc();

    List<Block> findAllByOrderByIndexAsc();
}
