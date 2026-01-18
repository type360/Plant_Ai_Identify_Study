package com.briup.pai.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.briup.pai.common.constant.LoginConstant;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.enums.UserStatusEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.common.utils.JwtUtil;
import com.briup.pai.common.utils.RedisUtil;
import com.briup.pai.common.utils.SecurityUtil;
import com.briup.pai.convert.UserConvert;
import com.briup.pai.entity.dto.LoginWithPhoneDTO;
import com.briup.pai.entity.dto.LoginWithUsernameDTO;
import com.briup.pai.entity.po.User;
import com.briup.pai.entity.vo.CurrentLoginUserVO;
import com.briup.pai.service.IAuthService;
import com.briup.pai.service.ILoginService;
import com.briup.pai.service.IUserService;
import jakarta.annotation.Resource;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

// loginService -> LoginMapper -> Login
// loginService -> UserService -> UserMapper -> User
@Service
public class LoginServiceImpl implements ILoginService {
    @Autowired
    private IUserService userService;
    @Override
    public String loginWithUsername(LoginWithUsernameDTO dto) { // briup 000
        // 根据用户名查账号 where username = 'bruip'
        User user = BriupAssert.requireNotNull(userService, User::getUsername,
                dto.getUsername(), ResultCodeEnum.USER_NOT_EXIST);
        // 比对密文密码 briup + md5的加密值 5fa4d6fc78072f42e0b9817d310bcd35
        BriupAssert.requireEqual(DigestUtils.md5Hex(dto.getPassword()),user.getPassword(),ResultCodeEnum.PASSWORD_IS_WRONG);
        // 用户状态是否禁用
        BriupAssert.requireEqual(user.getStatus(), UserStatusEnum.AVAILABLE.getStatus(),ResultCodeEnum.USER_IS_DISABLED);
        // 产生令牌[用户id] 1
        Map<String, Object> map = new HashMap<>();
        map.put(LoginConstant.JWT_PAYLOAD_KEY,user.getId()); // id
        String token = JwtUtil.generateJwt(map);
        return token;
    }
//    @Autowired
//    private HttpServletRequest request;
    @Autowired
    private UserConvert userConvert;
    @Resource
    private IAuthService authService;
    @Override
    public CurrentLoginUserVO getCurrentUser() {
//       Integer userId = (Integer) request.getAttribute("userId");
//        UserEchoVO userById = userService.getUserById(userId);
        Integer userId = SecurityUtil.getUserId();
        User user = userService.getById(userId);
        CurrentLoginUserVO currentLoginUserVO = userConvert.po2CurrentLoginUserVO(user);
        // 用户按钮
        currentLoginUserVO.setButtons(authService.getUserButtonPermissionList(userId));
        // 用户路由
        return currentLoginUserVO;
    }
    @Autowired
    private RedisUtil redisUtil;
    @Override
    public void sendMessageCode(String telephone) {
        // 此处应该使用阿里云短信的工具发送验证码
        // VerifyCode19951154250:7377
        String key = LoginConstant.USER_SMS_VERIFY_CODE_PREFIX + telephone;
        // 手机号必须存在
        User user = BriupAssert.requireNotNull(userService, User::getTelephone,
                telephone, ResultCodeEnum.USER_NOT_EXIST);
        // 用户不能禁用
        BriupAssert.requireEqual(user.getStatus(), UserStatusEnum.AVAILABLE.getStatus(),ResultCodeEnum.USER_IS_DISABLED);
        // 该手机号只能有一个验证码 通过key能否找到value
        BriupAssert.requireFalse(redisUtil.existKey(key),ResultCodeEnum.USER_VERIFY_CODE_ALREADY_EXIST);

        // 将验证码存到redis中,并指定存活时间
        int value = RandomUtil.randomInt(1000, 9999);

        System.out.println("验证码 = " + value);
        redisUtil.set(key,value,LoginConstant.USER_SMS_VERIFY_CODE_EXPIRATION_TIME);
    }

    @Override
    public String loginWithTelephone(LoginWithPhoneDTO dto) {
        String telephone = dto.getTelephone();
        Integer code = dto.getCode();
        // 根据手机号查用户
        User user = BriupAssert.requireNotNull(userService, User::getTelephone, telephone, ResultCodeEnum.USER_NOT_EXIST);
        BriupAssert.requireEqual(user.getStatus(), UserStatusEnum.AVAILABLE.getStatus(),ResultCodeEnum.USER_IS_DISABLED);
        // 用户输入的验证码和redis中的验证码比对
        Integer redisCode = (Integer) redisUtil.get(LoginConstant.USER_SMS_VERIFY_CODE_PREFIX + telephone);
        BriupAssert.requireEqual(redisCode,code,ResultCodeEnum.USER_VERIFY_CODE_ERROR);
        // 把验证码删除
        redisUtil.delete(LoginConstant.USER_SMS_VERIFY_CODE_PREFIX + telephone);

        Map<String, Object> map = new HashMap<>();
        map.put(LoginConstant.JWT_PAYLOAD_KEY,user.getId()); // id
        String token = JwtUtil.generateJwt(map);
        return token;
    }
}