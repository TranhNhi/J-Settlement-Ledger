package com.example.J_Settlement.Ledger.config;

public class DBContextHolder {
    private static final ThreadLocal<String> contextHolder = new ThreadLocal<>();

    public static void setCurrentDb(String dbType) {
        System.out.println(">>> [ROUTING] THIẾT LẬP DATABASE: " + dbType);
        contextHolder.set(dbType);
    }

    public static String getCurrentDb() {
        String db = contextHolder.get();
        if (db == null) {
            // Log này sẽ báo cho Trang biết Context đã bị xóa quá sớm!
            System.err.println(">>> [WARNING] Context bị trống! Đang dùng mặc định: CENTRAL");
            return "CENTRAL";
        }
        return db;
    }

    public static void clear() {
        contextHolder.remove();
        System.out.println(">>> [ROUTING] ĐÃ DỌN DẸP THREAD (CLEARED)");
    }
}