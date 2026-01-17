package com.briup.pai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.briup.pai.common.enums.PermissionTypeEnum;
import com.briup.pai.convert.PermissionConvert;
import com.briup.pai.dao.PermissionMapper;
import com.briup.pai.entity.po.Permission;
import com.briup.pai.entity.vo.PageVO;
import com.briup.pai.entity.vo.PermissionPageVO;
import com.briup.pai.service.IPermissionService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements IPermissionService {

    @Resource
    private PermissionConvert permissionConvert;
    /**
     * 分页查找所有权限
     *
     * @param pageNum
     * @param pageSize
     * @return 权限树的分页
     * 一级一级查询 条理清晰,性能差
     * 一次查询所有,通过代码再来组装 推荐
     */
    @Override
    public PageVO<PermissionPageVO> getPermissionByPage(Long pageNum, Long pageSize) {
        IPage<Permission> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Permission> wrapper = Wrappers.<Permission>lambdaQuery()
                .eq(Permission::getType, PermissionTypeEnum.CATALOGUE.getType())
                .orderByAsc(Permission::getSort);
        // 先查询目录
        page = this.page(page, wrapper);
        List<PermissionPageVO> categoryList = permissionConvert.po2PermissionPageVOList(page.getRecords())
                .stream()
                .peek(cagegory -> { // 目录id
                    // 再查询分类
                    wrapper.clear();
                    wrapper.eq(Permission::getType, PermissionTypeEnum.MENU.getType())
                            .eq(Permission::getParentId,cagegory.getId()) // 分类的parentId == 目录id
                            .orderByAsc(Permission::getSort);
                    List<PermissionPageVO> menuList = permissionConvert.po2PermissionPageVOList(this.list(wrapper));
                    cagegory.setChildren(menuList);
                    // 再查询按钮
                    menuList.forEach(menu -> {
                        wrapper.clear();
                        wrapper.eq(Permission::getType, PermissionTypeEnum.BUTTON.getType())
                                .eq(Permission::getParentId,menu.getId()) // 按钮的parentId == 分类id
                                .orderByAsc(Permission::getSort);
                        List<PermissionPageVO> buttonList = permissionConvert.po2PermissionPageVOList(this.list(wrapper));
                        menu.setChildren(buttonList);
                    });
                })
                .toList();



        PageVO<PermissionPageVO> permissionPageVOPageVO= new PageVO<PermissionPageVO>();
        permissionPageVOPageVO.setTotal(page.getTotal());
        permissionPageVOPageVO.setData(categoryList);
        return permissionPageVOPageVO;
    }
}