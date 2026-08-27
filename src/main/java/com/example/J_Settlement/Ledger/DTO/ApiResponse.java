package com.example.J_Settlement.Ledger.DTO;
/*Để Frontend (React) xử lý dễ dàng, bạn nên trả về một cấu trúc phản hồi thống nhất
thay vì chỉ trả về một chuỗi String đơn lẻ.*/
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data; // Có thể chứa thông tin Block hoặc Transaction ID

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
}
