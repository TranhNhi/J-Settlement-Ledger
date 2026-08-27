package com.example.J_Settlement.Ledger.validator;

import com.example.J_Settlement.Ledger.Repository.WalletRepository;
import com.example.J_Settlement.Ledger.exception.InvalidSignatureException;
import com.example.J_Settlement.Ledger.model.Wallet;
import com.example.J_Settlement.Ledger.until.CryptoUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class SignatureValidator implements TransactionValidator {

    @Autowired
    private WalletRepository walletRepository;

    @Override
    public void validate(String from, String to, double amount, byte[] sig) {
        try {
            // Lấy DB đang active để tìm public key
            Wallet senderWallet = walletRepository.findById(from)
                    .orElseThrow(() -> new InvalidSignatureException("Không tìm thấy ví người gửi"));

            String dataToSign = CryptoUtil.getDataToSign(from, to, amount);
            boolean isValid = CryptoUtil.verifyECDSASig(
                    senderWallet.getPublicKey(), dataToSign, sig);

            if (!isValid) throw new InvalidSignatureException("Chữ ký không hợp lệ");

        } catch (InvalidSignatureException e) {
            throw e;
        } catch (Exception e) {
            throw new InvalidSignatureException("Lỗi xác thực chữ ký: " + e.getMessage());
        }
    }
}