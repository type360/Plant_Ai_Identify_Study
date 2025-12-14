package com.briup.pai.controller;

import com.briup.pai.common.constant.LoginConstant;
import com.briup.pai.common.result.Result;
import com.briup.pai.entity.dto.LoginWithPhoneDTO;
import com.briup.pai.entity.dto.LoginWithUsernameDTO;
import com.briup.pai.service.ILoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/login")
@Tag(name = "登录控制器")
public class LoginController {

    @Resource
    private ILoginService loginService;

    @PostMapping("/withUsername")
    @Operation(summary = "用户名密码登录")
    public Result loginWithUsername(
            @RequestBody
            @Valid LoginWithUsernameDTO dto) {
        return Result.success(loginService.loginWithUsername(dto));
    }

    //该方法没有参数，怎么知道当前用户是谁？
    // 拦截器取出id 存到session|request
    // 控制器/service取出session|request中的id
    // 使用Threadlocal对象替换request,因为线程不安全
    @GetMapping("/getCurrentUser")
    @Operation(summary = "获取当前登录的用户信息")
    public Result getCurrentUser() {
        return Result.success(loginService.getCurrentUser());
    }

    @PostMapping("/sendMessageCode")
    @Operation(summary = "发送短信验证码")
    public Result sendMessageCode(
            @RequestParam(value = "telephone")
            @Pattern(
                    regexp = LoginConstant.USER_TELEPHONE_REGEX,
                    message = "手机号码格式有误") String telephone) {
        loginService.sendMessageCode(telephone);
        return Result.success();
    }

    @PostMapping("/withTelephone")
    @Operation(summary = "手机号验证码登录")
    public Result loginWithPhone(
            @RequestBody
            @Valid LoginWithPhoneDTO dto) {
        return Result.success(loginService.loginWithTelephone(dto));
    }

    /*此处是前端处理：前端将令牌从请求头中移除了
    * 为什么是前端处理而不是后端处理呢？
    * 这里反应了JWT的弊端，即JWT一旦生成无法销毁，只能等他自动过期
    *
    * 通过后端接口请求，如果令牌时间很长，1个月
    * 登录获取令牌后，记录下来 注意令牌不要太长时间
    * 解决方案一：将令牌存到redis中，redis的key期限就是token期限
    *           注销用户，将redis中的token删除
    *           访问资源，比对redis中的令牌
    *           通过这种方式解决jwt无法销毁问题
    * 方案二：不用jwt,换别的token oatuh2框架支持内置token可以销毁[缺点是不能存id信息]
    *       登录成功后，将信息存到threadlocal 要获取用户身份，从threadlocal直接取出来
    *
    * 将令牌放到请求头携带到后端
    * 请求行
    * 请求头
    * 请求体
    * 后端共享多次请求的资源 request,session,application
    * 前端共享多次请求的资源 Cookie,sessionStorage,localStorage(全局的)
    *
    * 前端把令牌存在哪里？
    * 第一次请求，产生令牌
    * 第二次访问资源，携带【请求头】、【cookie】
    * */
    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public Result logout() {
        return Result.success();
    }
}