package com.example.fingerartbackend.constant;

public final class UserPunishmentType {

    public static final String ACCOUNT_BAN = "ACCOUNT_BAN";
    public static final String NO_ORDER = "NO_ORDER";
    public static final String NO_FORUM = "NO_FORUM";
    public static final String NO_PRODUCT = "NO_PRODUCT";
    public static final String NO_SKILL = "NO_SKILL";

    private UserPunishmentType() {
    }

    public static String label(String type) {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case ACCOUNT_BAN -> "封禁账号";
            case NO_ORDER -> "禁止下单";
            case NO_FORUM -> "禁止发帖";
            case NO_PRODUCT -> "禁止上架";
            case NO_SKILL -> "禁止技能";
            default -> type;
        };
    }
}
