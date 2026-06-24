package com.example.fingerartbackend.util;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 随机昵称生成器。
 * <p>
 * 用于用户注册时自动生成默认昵称，格式为「前缀 + 6 位随机数字」。
 * </p>
 */
public final class NicknameGenerator {

    /** 昵称前缀候选词 */
    private static final String[] PREFIXES = {
            "手作萌新", "巧手匠人", "创意达人", "匠心用户", "造物者", "织梦手艺人"
    };

    private NicknameGenerator() {
    }

    /**
     * 生成随机昵称，如「手作萌新384729」。
     *
     * @return 随机昵称
     */
    public static String randomNickname() {
        String prefix = PREFIXES[ThreadLocalRandom.current().nextInt(PREFIXES.length)];
        int suffix = ThreadLocalRandom.current().nextInt(100000, 999999);
        return prefix + suffix;
    }
}
