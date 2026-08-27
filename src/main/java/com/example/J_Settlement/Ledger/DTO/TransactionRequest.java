package com.example.J_Settlement.Ledger.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/*Lớp này dùng để hứng dữ liệu JSON gửi lên từ ví của người dùng. Nó khớp chính xác với những gì Frontend sẽ gửi.*/

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @JsonProperty("fromAddress")
    private String fromAddress;  // Public key ví người gửi

    @JsonProperty("toAddress")
    private String toAddress;    // Public key ví người nhận

    @JsonProperty("amount")
    private Double amount;

    @JsonProperty("signature")
    private String signature;    // Chữ ký hex DER từ FE

    @JsonProperty("senderBank")
    private String senderBank;   // VCB, BIDV... — lấy từ JWT hoặc FE gửi lên

    @JsonProperty("recipientBank")
    private String recipientBank; // Ngân hàng người nhận — FE chọn từ dropdown
}