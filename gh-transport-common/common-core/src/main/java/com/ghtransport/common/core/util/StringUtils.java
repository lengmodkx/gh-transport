package com.ghtransport.common.core.util;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 */
public class StringUtils extends org.apache.commons.lang3.StringUtils {

    /**
     * 安全随机数生成器（密码学安全）
     */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private StringUtils() {
    }

    /**
     * 判断字符串是否为空（null、""、空格）
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否非空
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 判断字符串是否为空（仅null和""）
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否非空（仅null和""）
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否为纯数字
     */
    public static boolean isNumeric(String str) {
        if (isBlank(str)) {
            return false;
        }
        return Pattern.matches("\\d+", str);
    }

    /**
     * 判断字符串是否为字母
     */
    public static boolean isAlpha(String str) {
        if (isBlank(str)) {
            return false;
        }
        return Pattern.matches("[a-zA-Z]+", str);
    }

    /**
     * 判断字符串是否为字母和数字
     */
    public static boolean isAlphanumeric(String str) {
        if (isBlank(str)) {
            return false;
        }
        return Pattern.matches("[a-zA-Z0-9]+", str);
    }

    /**
     * 判断是否为有效的手机号
     */
    public static boolean isMobile(String mobile) {
        if (isBlank(mobile)) {
            return false;
        }
        // 中国手机号正则（简单版）
        return Pattern.matches("^1[3-9]\\d{9}$", mobile);
    }

    /**
     * 判断是否为有效的邮箱
     */
    public static boolean isEmail(String email) {
        if (isBlank(email)) {
            return false;
        }
        return Pattern.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", email);
    }

    /**
     * 判断是否为有效的身份证号
     */
    public static boolean isIdCard(String idCard) {
        if (isBlank(idCard)) {
            return false;
        }
        // 18位身份证号正则
        return Pattern.matches("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[1-2]\\d|3[0-1])\\d{3}[\\dXx]$", idCard);
    }

    /**
     * 去除字符串两端的空白
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 如果字符串为空，返回默认值
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    /**
     * 如果字符串为空，返回空字符串
     */
    public static String defaultIfBlank(String str) {
        return defaultIfBlank(str, "");
    }

    /**
     * 截取字符串（超出部分用省略号表示）
     */
    public static String truncate(String str, int maxLength, String suffix) {
        if (isBlank(str)) {
            return str;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength) + suffix;
    }

    /**
     * 截取字符串（超出部分用省略号表示，默认...）
     */
    public static String truncate(String str, int maxLength) {
        return truncate(str, maxLength, "...");
    }

    /**
     * 隐藏字符串中间部分
     * 例如：手机号 138****8888
     */
    public static String hideMiddle(String str, int start, int end, String replacement) {
        if (isBlank(str) || str.length() < start + end) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(str, 0, start);
        for (int i = 0; i < str.length() - start - end; i++) {
            sb.append(replacement);
        }
        sb.append(str, str.length() - end, str.length());
        return sb.toString();
    }

    /**
     * 隐藏手机号中间四位
     */
    public static String hideMobile(String mobile) {
        if (isBlank(mobile) || mobile.length() != 11) {
            return mobile;
        }
        return hideMiddle(mobile, 3, 4, "*");
    }

    /**
     * 隐藏身份证号中间部分
     */
    public static String hideIdCard(String idCard) {
        if (isBlank(idCard)) {
            return idCard;
        }
        if (idCard.length() == 18) {
            return hideMiddle(idCard, 3, 4, "*");
        } else if (idCard.length() == 15) {
            return hideMiddle(idCard, 3, 3, "*");
        }
        return idCard;
    }

    /**
     * 将驼峰命名转换为下划线命名
     * 例如：userName -> user_name
     */
    public static String camelToSnake(String str) {
        if (isBlank(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0) {
                    result.append("_");
                }
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * 将下划线命名转换为驼峰命名
     * 例如：user_name -> userName
     */
    public static String snakeToCamel(String str) {
        if (isBlank(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean nextUpperCase = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '_') {
                nextUpperCase = true;
            } else {
                if (nextUpperCase) {
                    result.append(Character.toUpperCase(c));
                    nextUpperCase = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        return result.toString();
    }

    /**
     * 首字母大写
     */
    public static String capitalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 首字母小写
     */
    public static String uncapitalize(String str) {
        if (isBlank(str)) {
            return str;
        }
        return Character.toLowerCase(str.charAt(0)) + str.substring(1);
    }

    /**
     * 重复字符串
     */
    public static String repeat(String str, int repeatCount) {
        if (isBlank(str) || repeatCount <= 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder(str.length() * repeatCount);
        for (int i = 0; i < repeatCount; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * 拼接字符串（忽略空值）
     */
    public static String joinIgnoreEmpty(String delimiter, String... parts) {
        List<String> list = new ArrayList<>();
        for (String part : parts) {
            if (isNotBlank(part)) {
                list.add(part);
            }
        }
        return String.join(delimiter, list);
    }

    /**
     * 将字符串转换为指定字符集
     */
    public static String convertCharset(String str, String sourceCharset, String targetCharset) {
        if (isBlank(str)) {
            return str;
        }
        try {
            return new String(str.getBytes(sourceCharset), targetCharset);
        } catch (Exception e) {
            return str;
        }
    }

    /**
     * UTF-8 转 ISO-8859-1
     */
    public static String utf8ToIso88591(String str) {
        return convertCharset(str, StandardCharsets.UTF_8.name(), StandardCharsets.ISO_8859_1.name());
    }

    /**
     * ISO-8859-1 转 UTF-8
     */
    public static String iso88591ToUtf8(String str) {
        return convertCharset(str, StandardCharsets.ISO_8859_1.name(), StandardCharsets.UTF_8.name());
    }

    /**
     * 生成随机字符串（使用SecureRandom，密码学安全）
     */
    public static String randomString(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 生成随机数字（使用SecureRandom，密码学安全）
     */
    public static String randomNumeric(int length) {
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(SECURE_RANDOM.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /**
     * 移除字符串中的所有空白字符
     */
    public static String removeWhitespace(String str) {
        if (isBlank(str)) {
            return str;
        }
        return str.replaceAll("\\s+", "");
    }

    /**
     * 判断字符串是否包含中文
     */
    public static boolean containsChinese(String str) {
        if (isBlank(str)) {
            return false;
        }
        return Pattern.compile("[\u4e00-\u9fa5]").matcher(str).find();
    }

    /**
     * 获取字符串的字节长度
     */
    public static int byteLength(String str) {
        if (isBlank(str)) {
            return 0;
        }
        return str.getBytes(StandardCharsets.UTF_8).length;
    }

    /**
     * 按字节长度截取字符串
     */
    public static String substringByByte(String str, int maxByteLength, String suffix) {
        if (isBlank(str) || maxByteLength <= 0) {
            return "";
        }
        byte[] bytes = str.getBytes(StandardCharsets.UTF_8);
        if (bytes.length <= maxByteLength) {
            return str;
        }
        int suffixByteLength = suffix.getBytes(StandardCharsets.UTF_8).length;
        int actualLength = maxByteLength - suffixByteLength;
        if (actualLength < 0) {
            return suffix;
        }
        StringBuilder sb = new StringBuilder();
        int currentLength = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            currentLength += String.valueOf(c).getBytes(StandardCharsets.UTF_8).length;
            if (currentLength <= actualLength) {
                sb.append(c);
            } else {
                break;
            }
        }
        return sb.toString() + suffix;
    }

    /**
     * 按字节长度截取字符串（默认...）
     */
    public static String substringByByte(String str, int maxByteLength) {
        return substringByByte(str, maxByteLength, "...");
    }
}
