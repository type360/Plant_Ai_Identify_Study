package com.briup.pai.interceptor;

import com.briup.pai.common.constant.LoginConstant;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.common.utils.JwtUtil;
import com.briup.pai.common.utils.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.messaging.handler.HandlerMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author 86151
 * @program: Plant_Ai_Identify_Study
 * @description
 * @create 2025/12/13 17:19
 **/
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是请求处理器方法，直接放行 放行跨域的预检请求 options currentUser()/get/options
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        // 请求头中没有token
        String token = request.getHeader(LoginConstant.TOKEN_NAME);
        BriupAssert.requireNotNull(
                token,
                ResultCodeEnum.USER_NOT_LOGIN
        );
        // 获取用户ID[如果查询当前用户需要此id的话]
        Integer userId = JwtUtil.getUserId(token);
        //request request.getSession()
//        request.setAttribute("userId", userId);
        SecurityUtil.setUserId(userId);

        // 放行
        return true;
    }

}
