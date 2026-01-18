package com.briup.pai.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.briup.pai.common.enums.PermissionTypeEnum;
import com.briup.pai.common.enums.ResultCodeEnum;
import com.briup.pai.common.exception.BriupAssert;
import com.briup.pai.common.utils.SecurityUtil;
import com.briup.pai.convert.PermissionConvert;
import com.briup.pai.convert.RoleConvert;
import com.briup.pai.entity.dto.AssignPermissionDTO;
import com.briup.pai.entity.dto.AssignRoleDTO;
import com.briup.pai.entity.po.*;
import com.briup.pai.entity.vo.AssignPermissionVO;
import com.briup.pai.entity.vo.AssignRoleVO;
import com.briup.pai.entity.vo.RouterVO;
import com.briup.pai.service.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class AuthServiceImpl implements IAuthService {
    @Resource
    private IRoleService roleService;
    @Resource
    private IUserRoleService userRoleService;
    @Resource
    private IUserService userService;
    @Resource
    private IPermissionService permissionService;
    @Resource
    private IRolePermissionService rolePermissionService;
    @Resource
    private RoleConvert roleConvert;
    @Resource
    private PermissionConvert permissionConvert;

    /**
     * 查询所有角色
     * 方便给用户分配角色
     */
    @Override
    public List<AssignRoleVO> getAllRoles() {
        return roleConvert.po2AssignRoleVOList(roleService.list());
    }

    /**
     * 查询用户已经存在的角色
     * 将来分配角色的时候,可以看到用户已经存在什么角色,还可以再分配什么角色
     */
    @Override
    public List<Integer> getRoleIdsByUserId(Integer userId) {
        // 用户必须存在
        BriupAssert.requireNotNull(
                userService,
                User::getId,
                userId,
                ResultCodeEnum.DATA_NOT_EXIST
        );
        // 根据用户id查询桥表的角色id
        return userRoleService.list(Wrappers.<UserRole>lambdaQuery()
                        .eq(UserRole::getUserId, userId))
                .stream()
                .map(UserRole::getRoleId)
                .toList();
    }

    /**
     * 给用户分配角色
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void assignRole(AssignRoleDTO dto) {
        Integer userId = dto.getUserId();
        List<Integer> roleIds = dto.getRoleIds();
        // 用户必须存在
        BriupAssert.requireNotNull(
                userService,
                User::getId,
                userId,
                ResultCodeEnum.DATA_NOT_EXIST
        );
        // 角色必须存在 数据库5 包含 参数3
        List<Integer> idsAll = roleService.list().stream().map(Role::getId).toList();
        BriupAssert.requireTrue(idsAll.containsAll(roleIds), ResultCodeEnum.DATA_NOT_EXIST);
        // 不能给自己分配角色
        BriupAssert.requireNotEqual(userId, SecurityUtil.getUserId(), ResultCodeEnum.ASSIGN_ROLE_ERROR);
        // 删除用户原有的角色
        userRoleService.remove(Wrappers.<UserRole>lambdaQuery().eq(UserRole::getUserId, userId));
        // 绑定新的角色 userId + roleIds
        List<UserRole> userRoleList = roleIds.stream().map(roleId -> {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            return userRole;
        }).toList();
        userRoleService.saveBatch(userRoleList);
    }

    /**
     * 查询所有权限
     * 方便分配权限
     */
    @Override
    public List<AssignPermissionVO> getAllPermissions() {
        // 查询分类
        LambdaQueryWrapper<Permission> wrapper = Wrappers.<Permission>lambdaQuery()
                .eq(Permission::getType, PermissionTypeEnum.CATALOGUE.getType())
                .orderByAsc(Permission::getSort);
        List<AssignPermissionVO> categoryList =
                permissionConvert.po2AssignPermissionVOList(permissionService.list(wrapper));
        categoryList.forEach(cagegory -> {
            // 再查询菜单
            wrapper.clear();
            wrapper.eq(Permission::getType, PermissionTypeEnum.MENU.getType())
                    .eq(Permission::getParentId, cagegory.getPermissionId()) // 分类的parentId == 目录id
                    .orderByAsc(Permission::getSort);
            List<AssignPermissionVO> menuList = permissionConvert.po2AssignPermissionVOList(permissionService.list(wrapper));
            cagegory.setChildren(menuList);
            menuList.forEach(menu -> {
                wrapper.clear();
                wrapper.eq(Permission::getType, PermissionTypeEnum.BUTTON.getType())
                        .eq(Permission::getParentId, menu.getPermissionId()) // 分类的parentId == 目录id
                        .orderByAsc(Permission::getSort);
                List<AssignPermissionVO> buttonList = permissionConvert.po2AssignPermissionVOList(permissionService.list(wrapper));
                menu.setChildren(buttonList);
            });

        });
        return categoryList;
    }

    /**
     * 查询角色原有的按钮权限id 前端树形组件可以自行推断
     * 方便树形结构回显
     */
    @Override
    public List<Integer> getPermissionIdsByRoleId(Integer roleId) {
        // 角色必须存在
        BriupAssert.requireNotNull(
                roleService,
                Role::getId,
                roleId,
                ResultCodeEnum.DATA_NOT_EXIST
        );
        // 查询角色拥有的按钮权限id即可
        List<Integer> allPermissionIds = rolePermissionService.list(Wrappers.<RolePermission>lambdaQuery()
                        .eq(RolePermission::getRoleId, roleId))
                .stream().map(RolePermission::getPermissionId)
                .toList();
        if(CollectionUtil.isEmpty(allPermissionIds)) {
            return allPermissionIds;
        }
        List<Integer> filterPermissionIds = permissionService.listByIds(allPermissionIds)
                .stream()
                .filter(permission ->
                        Objects.equals(permission.getType(), PermissionTypeEnum.BUTTON.getType()))
                .map(Permission::getId)
                .toList();
        return filterPermissionIds;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void assignPermission(AssignPermissionDTO dto) {
        // 角色必须存在
        Integer roleId = dto.getRoleId();
        BriupAssert.requireNotNull(
                roleService,
                Role::getId,
                roleId,
                ResultCodeEnum.DATA_NOT_EXIST
        );
        // 权限必须都存在
        List<Integer> permissionIds = dto.getPermissionIds();
        List<Integer> permissionIdList = permissionService
                .list()
                .stream()
                .map(Permission::getId)
                .toList();
        BriupAssert.requireTrue(
                CollectionUtil.containsAll(permissionIdList, permissionIds),
                ResultCodeEnum.DATA_NOT_EXIST
        );
        // 删除原有的角色权限
        rolePermissionService.remove(Wrappers.<RolePermission>lambdaQuery()
                .eq(RolePermission::getRoleId, roleId));
        // 新增角色权限
        List<RolePermission> list = permissionIds.stream()
                .map(id -> {
                    RolePermission rolePermission = new RolePermission();
                    rolePermission.setRoleId(roleId);
                    rolePermission.setPermissionId(id);
                    return rolePermission;
                }).toList();
        rolePermissionService.saveBatch(list);
    }

    /**
     * 查询出左侧的路由[目录 菜单]
     * @param userId
     */
    @Override
    public List<RouterVO> getRouter(Integer userId) {
        return List.of();
    }

    /**
     * 查询出所有用户的权限标识符 dataset:add
     * @param userId
     */
    @Override
    public List<String> getUserButtonPermissionList(Integer userId) {
        List<String> list = new ArrayList<>();
        // userid -> role -> permission -> perm
        List<Integer> roleIds = userRoleService.list(Wrappers.<UserRole>lambdaQuery()
                        .eq(UserRole::getUserId, userId))
                .stream().map(UserRole::getRoleId)
                .toList();
        roleIds.forEach(roleId->{
            List<Integer> pids = this.getPermissionIdsByRoleId(roleId);
            pids.forEach(pid->{
                Permission permission = permissionService.getById(pid);
                String perm = permission.getPerm();
                list.add(perm);
            });
        });

        return list;
    }
}