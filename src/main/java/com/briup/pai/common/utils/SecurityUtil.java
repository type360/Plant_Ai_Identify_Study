package com.briup.pai.common.utils;

/**
 * @author 86151
 * @program: Plant_Ai_Identify_Study
 * @description:Threadlocal工具类 每个线程都会有自己的变量。这个工具类要自己写
 * @create 2025/12/13 17:21
 * 请求顺序是 拦截器preHandle [控制器 service dao] 拦截器postHandle 之后有个渲染视图用的afterCompletion但这里用不到
 * 先在prehandle存储用户id,接着控制器和service层取
 *
 **/
public class SecurityUtil {

    private static final ThreadLocal<Integer> threadLocal = new ThreadLocal<>();

    //2.service取出threadlocal里的值
    public static Integer getUserId() {
        return threadLocal.get();
    }

    //1.preHandle拦截器取出token里的id,存到Threadlocal里
    public static void setUserId(Integer userId) {
        threadLocal.set(userId);
    }

    //销毁Threadlocal中的值 用完就删掉
    public static void removeUserId() {
        threadLocal.remove();
    }
}
