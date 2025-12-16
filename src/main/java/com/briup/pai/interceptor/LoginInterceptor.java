package com.briup.pai.interceptor;

import com.briup.pai.common.constant.LoginConstant;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.common.utils.JwtUtil;
import com.briup.pai.common.utils.SecurityUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 如果不是请求处理器方法，直接放行 放行跨域的预检请求 options currentUser()/get/options
//        request.getMethod().equals("OPTIONs")
        //放行预检请求（因为预检请求的处理器通常不是HandlerMethod）,同时预检请求内没有认证信息
        //放行其他非HandlerMethod的请求（比如静态资源请求)
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }
        // 请求头中没有token
        String token = request.getHeader(LoginConstant.TOKEN_NAME);
        BriupAssert.requireNotNull(
                token,
                ResultCodeEnum.USER_NOT_LOGIN
        );
        // 获取用户ID [如果查询当前用户需要此id的话]
        Integer userId = JwtUtil.getUserId(token);
        // request reqeust.getSession()
//        request.setAttribute("userId",userId);
        SecurityUtil.setUserId(userId);
        // 放行
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        SecurityUtil.removeUserId();
    }

}