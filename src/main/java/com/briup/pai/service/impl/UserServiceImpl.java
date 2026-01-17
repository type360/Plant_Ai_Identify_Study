package com.briup.pai.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.briup.pai.common.constant.LoginConstant;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.enums.UserStatusEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.common.utils.SecurityUtil;
import com.briup.pai.convert.UserConvert;
import com.briup.pai.dao.UserMapper;
import com.briup.pai.entity.dto.UserSaveDTO;
import com.briup.pai.entity.po.User;
import com.briup.pai.entity.po.UserRole;
import com.briup.pai.entity.vo.PageVO;
import com.briup.pai.entity.vo.UserEchoVO;
import com.briup.pai.entity.vo.UserPageVO;
import com.briup.pai.service.IUserRoleService;
import com.briup.pai.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Resource
    private UserConvert userConvert;

    @Override
    public String getUsernameById(Integer userId) {
        return this.getById(userId).getUsername();
    }

    @Override
    public PageVO<UserPageVO> getUserByPageAndCondition(Long pageNum, Long pageSize, String keyword) {
        // 构建分页对象
        Page<User> page = new Page<>(pageNum, pageSize);
        // 设置查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper
                .like(ObjectUtil.isNotEmpty(keyword), User::getUsername, keyword)
                .or()
                .like(ObjectUtil.isNotEmpty(keyword), User::getRealName, keyword)
                .or()
                .like(ObjectUtil.isNotEmpty(keyword), User::getTelephone, keyword)
                .orderByDesc(User::getCreateTime);
        Page<User> userPage = this.page(page, wrapper);
        // 构建PageVO对象
        PageVO<UserPageVO> userPageVO = new PageVO<>();
        userPageVO.setTotal(userPage.getTotal());
        userPageVO.setData(userConvert.po2UserPageVOList(userPage.getRecords()));
        return userPageVO;
    }

    @Override
    public UserEchoVO addOrModifyUser(UserSaveDTO dto) {
        Integer userId = dto.getUserId();
        User user;
        if (ObjectUtil.isEmpty(userId)) {
            // 用户名不能重复
            BriupAssert.requireNull(
                    this,
                    User::getUsername,
                    dto.getUsername(),
                    ResultCodeEnum.DATA_ALREADY_EXIST
            );
            // 转换对象
            user = userConvert.userSaveDTO2Po(dto);
            user.setPassword(
                    DigestUtils.md5DigestAsHex(
                            LoginConstant.INIT_PASSWORD.getBytes(StandardCharsets.UTF_8)
                    )
            );
            user.setStatus(UserStatusEnum.AVAILABLE.getStatus());
            user.setHeaderUrl(LoginConstant.INIT_HEADER_URL);
            user.setCreateTime(new Date());
            this.save(user);
        } else {
            // 用户必须存在
            BriupAssert.requireNotNull(
                    this,
                    User::getId,
                    dto.getUserId(),
                    ResultCodeEnum.DATA_NOT_EXIST
            );
            // 只能修改自己的账号
            BriupAssert.requireEqual(
                    SecurityUtil.getUserId(),
                    dto.getUserId(),
                    ResultCodeEnum.PARAM_IS_ERROR
            );
            // 用户名不能重复
            BriupAssert.requireNull(
                    this,
                    User::getUsername,
                    dto.getUsername(),
                    User::getId,
                    dto.getUserId(),
                    ResultCodeEnum.DATA_ALREADY_EXIST
            );
            user = userConvert.userSaveDTO2Po(dto);
            this.updateById(user);
        }
        return userConvert.po2UserEchoVO(user);
    }

    @Override
    public String uploadProfilePicture(MultipartFile file) {
        return "";
    }

    @Override
    public UserEchoVO getUserById(Integer userId) {
        return null;
    }

    @Override
    public void resetPassword(Integer userId) {
        // 用户必须存在
        User user = BriupAssert.requireNotNull(
                this,
                User::getId,
                userId,
                ResultCodeEnum.DATA_NOT_EXIST
        );
        // 重置密码
        user.setPassword(
                DigestUtils.md5DigestAsHex(
                        LoginConstant.INIT_PASSWORD.getBytes(StandardCharsets.UTF_8)
                )
        );
        this.updateById(user);
    }

    @Resource
    private IUserRoleService userRoleService;
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeUserById(Integer userId) {
        // 不能自我删除
        BriupAssert.requireNotEqual(userId,SecurityUtil.getUserId(),ResultCodeEnum.DATA_CAN_NOT_DELETE);
        // 用户必须存在
        BriupAssert.requireNotNull(this, User::getId, userId, ResultCodeEnum.DATA_NOT_EXIST);
        // 删除用户-角色信息  桥表存储了 1: [1,2,3]
        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getUserId, userId);
        userRoleService.remove(userRoleWrapper);
        this.removeById(userId);
    }

    @Override
    public void disableOrEnableUser(Integer userId, Integer status) {

    }
}