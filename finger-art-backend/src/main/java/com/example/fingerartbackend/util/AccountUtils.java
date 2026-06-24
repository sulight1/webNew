package com.example.fingerartbackend.util;

/**
 * 用户数字账号工具类。
 * <p>
 * 提供固定格式账号生成及注册时的数字账号格式校验。
 * </p>
 */
public final class AccountUtils {

    private AccountUtils() {
    }

    /**
     * 为已有用户生成固定数字账号：10000001、10000002 …
     *
     * @param userId 用户 ID
     * @return 数字账号字符串
     */
    public static String fixedAccountForUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        return String.valueOf(10000000L + userId);
    }

    /**
     * 校验数字账号格式：非空、纯数字、长度 6-20 位。
     *
     * @param account 待校验账号
     */
    public static void validateNumericAccount(String account) {
        if (account == null || account.isBlank()) {
            throw new RuntimeException("账号不能为空");
        }
        String trimmed = account.trim();
        if (!trimmed.matches("\\d+")) {
            throw new RuntimeException("账号只能由数字组成");
        }
        if (trimmed.length() < 6 || trimmed.length() > 20) {
            throw new RuntimeException("账号长度应为 6-20 位数字");
        }
    }
}
