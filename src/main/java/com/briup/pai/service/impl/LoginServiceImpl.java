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
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class LoginServiceImpl implements ILoginService {
    @Autowired
    private IUserService userService;
    @Resource
    private IAuthService authService;
    @Autowired
    private UserConvert userConvert;

    @Override
    @Transactional
    public String loginWithUsername(LoginWithUsernameDTO dto) {
        //这里要使用LoginServiceImpl->UserServiceImpl->UserMapper->User
        //对比用户名称判断用户名称是否存在
        User user = BriupAssert.requireNotNull(userService, User::getUsername, dto.getUsername(), ResultCodeEnum.USER_NOT_EXIST);
        //对比密码判断密码是否正确
        BriupAssert.requireEqual(DigestUtils.md5Hex(dto.getPassword()),user.getPassword(),ResultCodeEnum.PASSWORD_IS_WRONG);
        //对比用户状态吗，判断用户状态
        BriupAssert.requireEqual(user.getStatus(), UserStatusEnum.AVAILABLE.getStatus(),ResultCodeEnum.USER_IS_DISABLED);
        Map<String, Object> map = new HashMap<>();
        map.put(LoginConstant.JWT_PAYLOAD_KEY, user.getId());
        String token = JwtUtil.generateJwt(map);
        return token;
    }

    @Autowired
    private HttpServletRequest request;
    @Override
    public CurrentLoginUserVO getCurrentUser() {
//        Integer userId = (Integer) request.getAttribute("userId");
        int userId = SecurityUtil.getUserId();
        User user = userService.getById(userId);
        // 转换对象
        CurrentLoginUserVO currentLoginUserVO = userConvert.po2CurrentLoginUserVO(user);
        // 封装菜单和按钮权限
        currentLoginUserVO.setMenu(authService.getRouter(userId));
        currentLoginUserVO.setButtons(authService.getUserButtonPermissionList(userId));
        return userConvert.po2CurrentLoginUserVO(user);
    }

    @Autowired
    private RedisUtil redisUtil;
    @Override
    public void sendMessageCode(String telephone) {
        //此处应该使用阿里云短信工具发送验证码
        //VerifyCode19951154250:7377这是验证码键值对样例
        String key = LoginConstant.USER_SMS_VERIFY_CODE_PREFIX + telephone;
        //手机号必须存在
        User user = BriupAssert.requireNotNull(userService, User::getTelephone, telephone, ResultCodeEnum.USER_NOT_EXIST);
        //用户不能禁用
        BriupAssert.requireEqual(user.getStatus(), UserStatusEnum.AVAILABLE.getStatus(),ResultCodeEnum.USER_IS_DISABLED);
        //该手机号只能有一个验证码
        BriupAssert.requireFalse(redisUtil.existKey(key),ResultCodeEnum.USER_VERIFY_CODE_ALREADY_EXIST);
        //将验证码存到Redis中去，并制定存活时间
        int value = RandomUtil.randomInt(1000, 9999);

        redisUtil.set(key,value,LoginConstant.USER_SMS_VERIFY_CODE_EXPIRATION_TIME);

    }

    @Override
    public String loginWithTelephone(LoginWithPhoneDTO dto) {
        String telephone = dto.getTelephone();
        Integer code = dto.getCode();
        //根据手机号查用户
        User user = BriupAssert.requireNotNull(userService, User::getTelephone, telephone, ResultCodeEnum.USER_NOT_EXIST);
        //用户输入的验证码和Redis中的验证码比对
        Integer redisCode = (Integer)redisUtil.get(LoginConstant.USER_SMS_VERIFY_CODE_PREFIX + telephone);
        BriupAssert.requireEqual(redisCode,code,ResultCodeEnum.USER_VERIFY_CODE_ERROR);
        //把验证码删除
        redisUtil.delete(LoginConstant.USER_SMS_VERIFY_CODE_PREFIX + telephone);
        Map<String, Object> map = new HashMap<>();
        map.put(LoginConstant.JWT_PAYLOAD_KEY, user.getId());
        String token = JwtUtil.generateJwt(map);
        return token;
    }
}