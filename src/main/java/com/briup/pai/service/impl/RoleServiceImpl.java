package com.briup.pai.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.convert.RoleConvert;
import com.briup.pai.dao.RoleMapper;
import com.briup.pai.entity.dto.RoleSaveDTO;
import com.briup.pai.entity.po.Role;
import com.briup.pai.entity.po.RolePermission;
import com.briup.pai.entity.po.UserRole;
import com.briup.pai.entity.vo.RoleQueryVO;
import com.briup.pai.service.IRolePermissionService;
import com.briup.pai.service.IRoleService;
import com.briup.pai.service.IUserRoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {

    @Resource
    private RoleConvert roleConvert;
    @Resource
    private IUserRoleService userRoleService;
    @Resource
    private IRolePermissionService rolePermissionService;

    @Override
    public List<RoleQueryVO> getAllRoles() {
        return roleConvert.po2RoleQueryVOList(this.list());
    }

    @Override
    public RoleQueryVO addOrModifyRole(RoleSaveDTO roleSaveDTO) {
        Role role;
        Integer roleId = roleSaveDTO.getRoleId();
        if (ObjectUtil.isEmpty(roleId)) {
            // 角色名称不能重复
            BriupAssert.requireNull(
                    this,
                    Role::getRoleName,
                    roleSaveDTO.getRoleName(),
                    ResultCodeEnum.DATA_ALREADY_EXIST
            );
            // 转换实体保存
            role = roleConvert.roleSaveDTO2Po(roleSaveDTO);
            this.save(role);
        } else {
            // 角色必须存在
            BriupAssert.requireNotNull(
                    this,
                    Role::getId,
                    roleId,
                    ResultCodeEnum.DATA_NOT_EXIST
            );
            // 角色名称不能重复
            BriupAssert.requireNull(
                    this,
                    Role::getRoleName,
                    roleSaveDTO.getRoleName(),
                    Role::getId,
                    roleId,
                    ResultCodeEnum.DATA_ALREADY_EXIST
            );
            // 转换实体修改
            role = roleConvert.roleSaveDTO2Po(roleSaveDTO);
            this.updateById(role);
        }
        return roleConvert.po2RoleQueryVO(role);
    }

    @Override
    public RoleQueryVO getRoleById(Integer roleId) {
        // 角色必须存在
        Role role = BriupAssert.requireNotNull(
                this,
                Role::getId,
                roleId,
                ResultCodeEnum.DATA_NOT_EXIST
        );
        // 转换对象保存
        return roleConvert.po2RoleQueryVO(role);
    }

    @Override
    public void removeRoleById(Integer roleId) {
        // 角色必须存在
        BriupAssert.requireNotNull(
                this,
                Role::getId,
                roleId,
                ResultCodeEnum.DATA_NOT_EXIST
        );
        // 删除对应的用户-角色信息
        LambdaQueryWrapper<UserRole> userRoleWrapper = new LambdaQueryWrapper<>();
        userRoleWrapper.eq(UserRole::getRoleId, roleId);
        userRoleService.remove(userRoleWrapper);
        // 删除对应的角色-权限信息
        LambdaQueryWrapper<RolePermission> rolePermissionWrapper = new LambdaQueryWrapper<>();
        rolePermissionWrapper.eq(RolePermission::getRoleId, roleId);
        rolePermissionService.remove(rolePermissionWrapper);
        // 删除角色
        this.removeById(roleId);
    }
}