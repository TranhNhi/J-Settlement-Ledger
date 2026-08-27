package com.example.J_Settlement.Ledger.until;

import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.util.Arrays;


public class CryptoUtil {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    // ----------------------------------------------------------------
    // SHA-256 — dùng cho hash block và state root
    // ----------------------------------------------------------------
    public static String applySha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ----------------------------------------------------------------
    // Format dữ liệu ký — PHẢI KHỚP VỚI FE
    // FE ký: fromAddress + toAddress + amount (không có timestamp)
    // ----------------------------------------------------------------
    public static String getDataToSign(String from, String to, double amount) {
        String amountStr = new java.math.BigDecimal(amount)
                .stripTrailingZeros()
                .toPlainString();
        if (!amountStr.contains(".")) {
            amountStr = amountStr + ".0";
        }
        return from + to + amountStr;
    }

    // ----------------------------------------------------------------
    // Verify chữ ký ECDSA secp256k1 từ FE gửi lên
    // publicKeyHex: chuỗi hex từ DB (do FE generate bằng elliptic)
    // dataToSign  : from + to + amount
    // signatureHex: chuỗi hex DER từ FE gửi lên
    // ----------------------------------------------------------------
    public static boolean verifyECDSASig(String publicKeyHex, String dataToSign, byte[] signatureBytes) {
        System.out.println(">>> [VERIFY] dataToSign: " + dataToSign);
        try {
            // Setup secp256k1
            org.bouncycastle.asn1.x9.X9ECParameters curveParams =
                    org.bouncycastle.asn1.sec.SECNamedCurves.getByName("secp256k1");
            org.bouncycastle.crypto.params.ECDomainParameters domainParams =
                    new org.bouncycastle.crypto.params.ECDomainParameters(
                            curveParams.getCurve(), curveParams.getG(),
                            curveParams.getN(), curveParams.getH());

            // Decode public key
            byte[] pubKeyBytes = hexToBytes(publicKeyHex);
            org.bouncycastle.math.ec.ECPoint point = curveParams.getCurve().decodePoint(pubKeyBytes);
            org.bouncycastle.crypto.params.ECPublicKeyParameters pubKey =
                    new org.bouncycastle.crypto.params.ECPublicKeyParameters(point, domainParams);

            // Parse r, s từ DER
            org.bouncycastle.asn1.ASN1Sequence seq =
                    org.bouncycastle.asn1.ASN1Sequence.getInstance(signatureBytes);
            java.math.BigInteger r = org.bouncycastle.asn1.ASN1Integer
                    .getInstance(seq.getObjectAt(0)).getPositiveValue();
            java.math.BigInteger s = org.bouncycastle.asn1.ASN1Integer
                    .getInstance(seq.getObjectAt(1)).getPositiveValue();

            // SHA256 hash
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(dataToSign.getBytes("UTF-8"));
            System.out.println(">>> [VERIFY] hash: " + bytesToHex(hash));

            // Verify
            org.bouncycastle.crypto.signers.ECDSASigner signer =
                    new org.bouncycastle.crypto.signers.ECDSASigner();
            signer.init(false, pubKey);
            boolean result = signer.verifySignature(hash, r, s);
            System.out.println(">>> [VERIFY] Result: " + result);
            return result;

        } catch (Exception e) {
            System.err.println("Verify lỗi: " + e.getMessage());
            return false;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
    // ----------------------------------------------------------------
    // Helper: chuyển hex string → byte array
    // ----------------------------------------------------------------
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}