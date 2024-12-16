package com.buaoye.oss.common.util;

/**
 * 字符串工具类
 *
 * @author Jayson Wu
 * @since 2024-12-13
 */
public class StringUtil {

    /**
     * 检查字符串是否为空
     *
     * @param str 字符串
     * @return True：空，False：非空
     */
    public static boolean isNullOrUndefined(String str) {
        return str == null || str.isEmpty() || "null".equalsIgnoreCase(str) || "undefined".equalsIgnoreCase(str);
    }

}
