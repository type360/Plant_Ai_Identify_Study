package com.briup.pai.common.utils;

/**
 * @Auther: vanse(lc)
 * @Date: 2025/11/6-11-06-下午2:19
 * @Description：ThreadLocal工具类 每个线程独有的变量
 *   拦截器preHandler  [控制器 service dao]    拦截器postHandler
 *   -- 渲染视图 afterCompletion
 */
public class SecurityUtil {
    private static ThreadLocal<Integer> threadLocal = new ThreadLocal<Integer>();

    // 1.拦截器preHandler  取出token里的id,存到ThreadLocal里
    public static void setUserId(Integer userId){
        threadLocal.set(userId);
    }

    // 2.service取出threadLocal里的值
    public static Integer getUserId(){
        return threadLocal.get();
    }

    // 3.销毁threalocal中的值 用完就删掉
    public static void removeUserId(){
        threadLocal.remove();
    }
}
