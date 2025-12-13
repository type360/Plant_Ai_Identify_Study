package com.briup.pai.common.utils;

/**
 * @author 86151
 * @program: Plant_Ai_Identify_Study
 * @description
 * @create 2025/12/13 17:21
 **/
public class SecurityUtil {

    private static final ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    public static Integer getUserId() {
        return threadLocal.get();
    }

    public static void setUserId(Integer userId) {
        if (threadLocal.get() == null) {
            threadLocal.set(userId);
        }
    }

    public static void removeUserId() {
        threadLocal.remove();
    }
}
