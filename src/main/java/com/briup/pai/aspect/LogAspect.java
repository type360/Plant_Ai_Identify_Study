package com.briup.pai.aspect;

import cn.hutool.core.util.DesensitizedUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.briup.pai.common.enums.LogTypeEnum;
import com.briup.pai.common.enums.RequestStatusEnum;
import com.briup.pai.common.result.Result;
import com.briup.pai.common.utils.IpUtil;
import com.briup.pai.common.utils.SecurityUtil;
import com.briup.pai.entity.dto.LoginWithPhoneDTO;
import com.briup.pai.entity.dto.LoginWithUsernameDTO;
import com.briup.pai.entity.po.Log;
import com.briup.pai.service.ILogService;
import com.briup.pai.service.IUserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@Aspect
@Slf4j
public class LogAspect {

    @Resource
    private ILogService logService;
    @Resource
    private IUserService userService;

    @Resource
    private HttpServletRequest request;

    // 除去所有的[get开头]查询方法，其余方法都需要添加日志
    @Pointcut("execution( * com.briup.pai.controller.*.*(..)) && !execution( * com.briup.pai.controller.*.get*(..))")
    public void pointCut() {
    }

    @Around("pointCut()")
    public Result around(ProceedingJoinPoint joinPoint) { // 执行目标方法
        Result result;
        // 记录日志信息  添加的日志数据表
        Log saveLog = new Log();
        // 开始时间 (end-start)
        Long startTime = System.currentTimeMillis();
        // 设置请求时间
        saveLog.setOperateTime(new Date());
        // 设置请求URI
        String requestURI = request.getRequestURI();
        saveLog.setRequestUri(requestURI);
        // 设置请求方式 get/post/put/delete
        saveLog.setRequestMethod(request.getMethod());
        // 设置请求IP地址 127.0.0.1 request.getRemoteAddr()
        saveLog.setRequestIp(IpUtil.getIpAddress(request));
        // 设置请求方法名称
        String methodName = joinPoint.getSignature().getName();
        log.info("【日志模块】：本次请求的URI为：{}，请求的方法名称为：{}", requestURI, methodName);
        saveLog.setMethodName(methodName);
        if (requestURI.contains("/login")) { // 登录日志 操作日志
            // 登录日志
            saveLog.setType(LogTypeEnum.LOGIN.getType()); // 2
            // 设置请求参数
            Object[] args = joinPoint.getArgs();
            if (ObjectUtil.isNotEmpty(args)) {
                Object arg = joinPoint.getArgs()[0];
                if (arg instanceof LoginWithUsernameDTO dto) {
                    // 用户名密码登录：需要对密码脱敏
                    LoginWithUsernameDTO loginWithUsernameDTO = new LoginWithUsernameDTO();
                    loginWithUsernameDTO.setUsername(dto.getUsername());
                    // 将密码变为****** 脱敏 hutool.DesensitizedUtil
                    loginWithUsernameDTO.setPassword(DesensitizedUtil.password(dto.getPassword()));
                    // 设置操作用户为用户名
//                    saveLog.setOperateUser(dto.getUsername());
                    // 设置请求参数
                    saveLog.setRequestParams(JSON.toJSONString(loginWithUsernameDTO));
                    log.info("【日志模块-登录日志-用户名密码登录】：本次请求的参数为{}", JSON.toJSONString(loginWithUsernameDTO));
                } else if (arg instanceof LoginWithPhoneDTO dto) {
                    // 手机号登录：需要对手机号脱敏
                    LoginWithPhoneDTO loginWithPhoneDTO = new LoginWithPhoneDTO();
                    // 175****1090
                    loginWithPhoneDTO.setTelephone(DesensitizedUtil.mobilePhone(dto.getTelephone()));
                    loginWithPhoneDTO.setCode(dto.getCode());
                    // 设置操作用户为用户名
                    saveLog.setOperateUser(DesensitizedUtil.mobilePhone(dto.getTelephone()));
                    // 设置请求参数
                    saveLog.setRequestParams(JSON.toJSONString(loginWithPhoneDTO));
                    log.info("【日志模块-登录日志-手机号验证码登录】：本次请求的参数为{}", JSON.toJSONString(loginWithPhoneDTO));
                } else {
                    String telephone = (String) arg;
                    // 发送短信验证码：设置操作的用户为手机号（需要信息脱敏）
                    String phone = DesensitizedUtil.mobilePhone(telephone);
                    saveLog.setOperateUser(phone);
                    // 设置请求参数
                    saveLog.setRequestParams(JSON.toJSONString(phone));
                    log.info("【日志模块-登录日志-获取短信验证码】：本次请求的参数为{}", JSON.toJSONString(phone));
                }
            } else {
                log.info("【日志模块-登录日志-退出登录】");
                // 退出登录接口
                saveLog.setOperateUser(userService.getUsernameById(SecurityUtil.getUserId()));
            }
        } else {
            // 操作日志
            saveLog.setType(LogTypeEnum.OPERATION.getType()); // 1
            // 设置操作的用户名
            saveLog.setOperateUser(userService.getUsernameById(SecurityUtil.getUserId()));
            // 设置请求参数 文件上传等非业务对象,不记录参数
            List<Object> validArgs = new ArrayList<>();
            for (Object arg : joinPoint.getArgs()) {
                // 需要过滤掉HttpServletResponse对象（否则导出excel时会报错）
                if (arg instanceof HttpServletResponse) {
                    continue;
                }
                // 如果参数是MultipartFile类型，请求参数就设置为文件类型名称
                if (arg instanceof MultipartFile file) {
                    validArgs.add(file.getClass().getName());
                    continue;
                }
                validArgs.add(arg);
            }
            if (ObjectUtil.isNotEmpty(validArgs)) {
                log.info("【日志模块-操作日志】：本次请求的参数为{}", validArgs);
                saveLog.setRequestParams(JSON.toJSONString(validArgs));
            }
        }
        try {
            // 设置响应数据 ***** 调用目标方法 获取方法的返回值 ******
            result = (Result) joinPoint.proceed();
            // {"code":200,"data":{"dictCode":"one","dictId":21,"dictValue":"one"},"message":"success"}
            saveLog.setResponseData(JSON.toJSONString(result));
            // 设置是否成功
            saveLog.setIsSuccess(RequestStatusEnum.SUCCESS.getStatus());
        } catch (Throwable e) { // aop将异常捕获了 [获取失败的信息]
            // 设置失败
            saveLog.setIsSuccess(RequestStatusEnum.FAIL.getStatus());
            // 设置异常信息
            saveLog.setResponseData(e.getMessage());
            // 抛出异常（交给全局异常捕获）
            throw new RuntimeException(e); // 页面正常统一响应失败信息
        } finally {
            // 设置请求时间
            Long endTime = System.currentTimeMillis();
            saveLog.setRequestTime(endTime - startTime);
            // 保存日志
            logService.save(saveLog);
        }
        return result;
    }
}