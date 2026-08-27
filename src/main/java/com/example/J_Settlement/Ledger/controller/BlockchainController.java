package com.example.J_Settlement.Ledger.controller;

import com.example.J_Settlement.Ledger.DTO.ApiResponse;
import com.example.J_Settlement.Ledger.Repository.BlockRepository;
import com.example.J_Settlement.Ledger.model.Block;
import com.example.J_Settlement.Ledger.service.IntegrityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/api/blockchain")
public class BlockchainController {

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private IntegrityService integrityService;

    // Lấy danh sách toàn bộ các khối để hiển thị lên UI
    @GetMapping("/blocks")
    public ResponseEntity<ApiResponse<List<Block>>> getChain() {
        List<Block> blocks = blockRepository.findAllByOrderByIndexAsc();
        return ResponseEntity.ok(ApiResponse.success("Tải dữ liệu chuỗi thành công", blocks));
    }

    // TÍNH NĂNG MỚI: Kiểm tra tính toàn vẹn của toàn bộ hệ thống
    @GetMapping("validate")
    public ResponseEntity<ApiResponse<String>> validate() {
        boolean isValid = integrityService.isChainValid();
        if (isValid) {
            return ResponseEntity.ok(ApiResponse.success("Hệ thống toàn vẹn. Dữ liệu chưa bị thay đổi.", "VALID"));
        } else {
            return ResponseEntity.status(400).body(ApiResponse.error("CẢNH BÁO: Dữ liệu Blockchain đã bị can thiệp trái phép!"));
        }
    }
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStatus() {
        Map<String, Object> data = new HashMap<>();
        data.put("connected", true);
        data.put("nodeName", "Vietcombank Ledger Node");
        data.put("totalBlocks", blockRepository.count());

        return ResponseEntity.ok(ApiResponse.success("Hệ thống trực tuyến", data));
    }
}