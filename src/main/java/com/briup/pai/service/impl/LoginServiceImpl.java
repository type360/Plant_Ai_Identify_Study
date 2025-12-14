package com.briup.pai.service.impl;

import com.briup.pai.common.constant.LoginConstant;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.enums.UserStatusEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.common.utils.JwtUtil;
import com.briup.pai.common.utils.SecurityUtil;
import com.briup.pai.convert.UserConvert;
import com.briup.pai.entity.dto.LoginWithPhoneDTO;
import com.briup.pai.entity.dto.LoginWithUsernameDTO;
import com.briup.pai.entity.po.User;
import com.briup.pai.entity.vo.CurrentLoginUserVO;
import com.briup.pai.service.ILoginService;
import com.briup.pai.service.IUserService;
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
    @Autowired
    private UserConvert userConvert;

    @Override
    public String loginWithUsername(LoginWithUsernameDTO dto) {
        //这里要使用LoginServiceImpl->UserServiceImpl->UserMapper->User
        //对比用户名称判断用户名称是否存在
        User user = BriupAssert.requireNotNull(userService, User::getUsername, dto.getUsername(), ResultCodeEnum.USER_NOT_EXIST);
        //对比密码判断密码是否正确
        BriupAssert.requireEqual(DigestUtils.md5Hex(dto.getPassword()),user.getPassword(),ResultCodeEnum.PASSWORD_IS_WRONG);
        //对比用户状态吗，判断用户状态
        BriupAssert.requireEqual(user.getStatus(), UserStatusEnum.AVAILABLE,ResultCodeEnum.USER_IS_DISABLED);
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
        return userConvert.po2CurrentLoginUserVO(user);
    }

    @Override
    public void sendMessageCode(String telephone) {

    }

    @Override
    public String loginWithTelephone(LoginWithPhoneDTO dto) {
        return "";
    }
}