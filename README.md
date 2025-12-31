🏦 J-Settlement Ledger - Blockchain-based Bank Settlement System
J-Settlement Ledger là một hệ thống mô phỏng mạng lưới quyết toán liên ngân hàng sử dụng công nghệ Blockchain. Dự án tập trung vào việc đảm bảo tính minh bạch, chống giả mạo và bảo mật tuyệt đối cho các giao dịch tài chính thông qua mật mã học đường cong Elliptic (ECDSA).

✨ Tính năng nổi bật
Decentralized Ledger: Sổ cái phân tán lưu trữ toàn bộ lịch sử giao dịch dưới dạng chuỗi khối (Blockchain).

Digital Signature (ECDSA): Mỗi ngân hàng (Bank Node) sở hữu cặp khóa Private/Public Key riêng để ký số và xác thực giao dịch, đảm bảo tính chống chối bỏ.

SHA-256 Hashing: Các khối được liên kết chặt chẽ bằng thuật toán băm SHA-256, khiến việc sửa đổi dữ liệu quá khứ là không thể.

Real-time Balance Audit: Tính toán số dư dựa trên lịch sử giao dịch (Transaction-based balance), ngăn chặn chi tiêu vượt mức (Double-spending).

RESTful API: Cung cấp các Endpoint chuẩn để tích hợp với các hệ thống Core Banking khác.

🛠 Công nghệ sử dụng
Language: Java 17

Framework: Spring Boot 3.x

Cryptography: Bouncy Castle (ECDSA, SHA-256)

Build Tool: Maven

Library: Lombok, Gson

🏗 Cấu trúc dự án
Plaintext

com.bank.settlement
├── controller      # REST API Endpoints
├── service         # Blockchain & Business Logic
├── model           # Data structures (Block, Transaction, BankNode)
├── util            # Cryptography Tools (SHA-256, ECDSA)
└── JSettlementApplication.java

🚀 Hướng dẫn khởi chạy
Clone dự án:

Bash git clone https://github.com/yourusername/j-settlement-ledger.git
Build dự án:
Bash mvn clean install -DskipTests
Chạy ứng dụng: Chạy file JSettlementApplication.java từ IDE của bạn (IntelliJ/Eclipse).

📡 API Documentation
1. Đăng ký ngân hàng mới
URL: /api/blockchain/banks

Method: POST

Params: name (String)

2. Thực hiện quyết toán (Chuyển tiền)
URL: /api/blockchain/transfer

Method: POST

Params: from (Tên ngân hàng gửi), to (Tên ngân hàng nhận), amount (Số tiền)

3. Xem sổ cái (Blockchain Ledger)
URL: /api/blockchain/ledger

Method: GET

🛡 Bảo mật & Toàn vẹn dữ liệu
Dự án này triển khai mô hình bảo mật đa lớp:

Hashing Integrity: Mỗi Block chứa previousHash. Nếu một giao dịch trong khối bị sửa đổi, mã hash sẽ thay đổi hoàn toàn, làm đứt gãy chuỗi.

Non-repudiation: Không ngân hàng nào có thể phủ nhận giao dịch mình đã thực hiện vì mỗi lệnh chuyển tiền yêu cầu chữ ký số được tạo ra từ chính Private Key của họ.
