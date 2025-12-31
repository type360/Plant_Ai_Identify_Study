package com.briup.pai.convert;

import com.briup.pai.entity.dto.RoleSaveDTO;
import com.briup.pai.entity.po.Role;
import com.briup.pai.entity.vo.AssignRoleVO;
import com.briup.pai.entity.vo.DropDownVO;
import com.briup.pai.entity.vo.RoleQueryVO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2025-12-16T14:38:30+0800",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.8 (Oracle Corporation)"
)
@Component
public class RoleConvertImpl implements RoleConvert {

    @Override
    public DropDownVO po2DropDownVO(Role role) {
        if ( role == null ) {
            return null;
        }

        DropDownVO dropDownVO = new DropDownVO();

        if ( role.getId() != null ) {
            dropDownVO.setKey( String.valueOf( role.getId() ) );
        }
        dropDownVO.setValue( role.getRoleName() );

        return dropDownVO;
    }

    @Override
    public List<DropDownVO> po2DropDownVOList(List<Role> roles) {
        if ( roles == null ) {
            return null;
        }

        List<DropDownVO> list = new ArrayList<DropDownVO>( roles.size() );
        for ( Role role : roles ) {
            list.add( po2DropDownVO( role ) );
        }

        return list;
    }

    @Override
    public RoleQueryVO po2RoleQueryVO(Role role) {
        if ( role == null ) {
            return null;
        }

        RoleQueryVO roleQueryVO = new RoleQueryVO();

        roleQueryVO.setRoleId( role.getId() );
        roleQueryVO.setRoleName( role.getRoleName() );
        roleQueryVO.setRoleDesc( role.getRoleDesc() );

        return roleQueryVO;
    }

    @Override
    public List<RoleQueryVO> po2RoleQueryVOList(List<Role> roles) {
        if ( roles == null ) {
            return null;
        }

        List<RoleQueryVO> list = new ArrayList<RoleQueryVO>( roles.size() );
        for ( Role role : roles ) {
            list.add( po2RoleQueryVO( role ) );
        }

        return list;
    }

    @Override
    public Role roleSaveDTO2Po(RoleSaveDTO roleSaveDTO) {
        if ( roleSaveDTO == null ) {
            return null;
        }

        Role role = new Role();

        role.setId( roleSaveDTO.getRoleId() );
        role.setRoleName( roleSaveDTO.getRoleName() );
        role.setRoleDesc( roleSaveDTO.getRoleDesc() );

        return role;
    }

    @Override
    public AssignRoleVO po2AssignRoleVO(Role role) {
        if ( role == null ) {
            return null;
        }

        AssignRoleVO assignRoleVO = new AssignRoleVO();

        assignRoleVO.setRoleId( role.getId() );
        assignRoleVO.setRoleName( role.getRoleName() );

        return assignRoleVO;
    }

    @Override
    public List<AssignRoleVO> po2AssignRoleVOList(List<Role> list) {
        if ( list == null ) {
            return null;
        }

        List<AssignRoleVO> list1 = new ArrayList<AssignRoleVO>( list.size() );
        for ( Role role : list ) {
            list1.add( po2AssignRoleVO( role ) );
        }

        return list1;
    }
}
