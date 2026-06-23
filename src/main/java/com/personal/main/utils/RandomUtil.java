package com.personal.main.utils;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtil {
    // 定义允许出现的字符集（数字 + 大小写字母）
    private static final String CHAR_POOL = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final int TARGET_LENGTH = 8;
    public static String generateRandomAccountId() {
        StringBuilder sb = new StringBuilder(TARGET_LENGTH);
        for (int i = 0; i < TARGET_LENGTH; i++) {
            int randomIndex = ThreadLocalRandom.current().nextInt(CHAR_POOL.length());
            sb.append(CHAR_POOL.charAt(randomIndex));
        }
        
        return sb.toString();
    }
}