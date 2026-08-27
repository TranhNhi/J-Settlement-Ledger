package com.example.J_Settlement.Ledger.service;

import com.example.J_Settlement.Ledger.Repository.BlockRepository;
import com.example.J_Settlement.Ledger.model.Block;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IntegrityService {
    private static final Logger logger = LoggerFactory.getLogger(IntegrityService.class);

    @Autowired
    private BlockRepository blockRepository;

    public boolean isChainValid() {
        // Lấy toàn bộ chuỗi sắp xếp theo index
        List<Block> blockchain = blockRepository.findAllByOrderByIndexAsc();

        for (int i = 0; i < blockchain.size(); i++) {
            Block currentBlock = blockchain.get(i);

            // 1. Kiểm tra tính toàn vẹn nội tại (Hash hiện tại có khớp với dữ liệu bên trong?)
            if (!currentBlock.getHash().equals(currentBlock.calculateHash())) {
                logger.error("CẢNH BÁO: Khối số {} đã bị sửa đổi dữ liệu!", currentBlock.getIndex());
                return false;
            }

            // 2. Kiểm tra tính liên kết (Trừ khối Genesis)
            if (i > 0) {
                Block previousBlock = blockchain.get(i - 1);
                if (!currentBlock.getPreviousHash().equals(previousBlock.getHash())) {
                    logger.error("CẢNH BÁO: Khối số {} bị đứt gãy liên kết với khối trước!", currentBlock.getIndex());
                    return false;
                }
            }
        }
        logger.info("Xác nhận: Chuỗi Blockchain hoàn toàn toàn vẹn.");
        return true;
    }
}
