package com.example.fingerartbackend.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Service;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

/**
 * 双因素认证服务接口，定义业务能力（业务服务接口）。
 */
@Service
public class TotpService {

    private static final String ISSUER = "指尖造物管理端";

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

    /**
     * 生成令牌或数据。
     */
    public String generateSecret() {
        return secretGenerator.generate();
    }

    /**
     * 构建响应对象。
     */
    public String buildOtpAuthUrl(String account, String secret) {
        QrData data = new QrData.Builder()
                .label(account)
                .secret(secret)
                .issuer(ISSUER)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        return data.getUri();
    }

    /**
     * 生成令牌或数据。
     */
    public String generateQrDataUri(String account, String secret) throws QrGenerationException {
        QrData data = new QrData.Builder()
                .label(account)
                .secret(secret)
                .issuer(ISSUER)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();
        QrGenerator generator = new ZxingPngQrGenerator();
        byte[] imageData = generator.generate(data);
        return getDataUriForImage(imageData, generator.getImageMimeType());
    }

    /**
     * 执行 verifyCode 相关逻辑。
     */
    public boolean verifyCode(String secret, String code) {
        if (secret == null || secret.isBlank() || code == null || code.isBlank()) {
            return false;
        }
        String normalized = code.trim().replaceAll("\\s+", "");
        if (!normalized.matches("\\d{6}")) {
            return false;
        }
        return codeVerifier.isValidCode(secret, normalized);
    }
}
