package com.example.fingerartbackend.util;

import java.util.concurrent.ThreadLocalRandom;

public final class NicknameGenerator {

    private static final String[] PREFIXES = {
            "手作萌新", "巧手匠人", "创意达人", "匠心用户", "造物者", "织梦手艺人"
    };

    private NicknameGenerator() {
    }

    public static String randomNickname() {
        String prefix = PREFIXES[ThreadLocalRandom.current().nextInt(PREFIXES.length)];
        int suffix = ThreadLocalRandom.current().nextInt(100000, 999999);
        return prefix + suffix;
    }
}
