package com.example.fingerartbackend.constant;

/**
 * 用户处罚类型常量。
 * <p>
 * 定义平台对用户违规行为的处罚种类，配合 {@code UserPunishment} 实体使用。
 * </p>
 */
public final class UserPunishmentType {

    /** 封禁账号（禁止登录及全部操作） */
    public static final String ACCOUNT_BAN = "ACCOUNT_BAN";

    /** 禁止下单 */
    public static final String NO_ORDER = "NO_ORDER";

    /** 禁止发帖/回复 */
    public static final String NO_FORUM = "NO_FORUM";

    /** 禁止上架作品 */
    public static final String NO_PRODUCT = "NO_PRODUCT";

    /** 禁止发布/交换技能 */
    public static final String NO_SKILL = "NO_SKILL";

    private UserPunishmentType() {
    }

    /**
     * 将处罚类型代码转换为中文展示标签。
     *
     * @param type 处罚类型常量
     * @return 中文标签，未知类型原样返回
     */
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
