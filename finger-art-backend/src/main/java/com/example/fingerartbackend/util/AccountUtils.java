package com.example.fingerartbackend.util;

public final class AccountUtils {

    private AccountUtils() {
    }

    /** 为已有用户生成固定数字账号：10000001、10000002 … */
    public static String fixedAccountForUserId(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        return String.valueOf(10000000L + userId);
    }

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
