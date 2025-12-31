package com.briup.pai.convert;

import com.briup.pai.entity.po.Permission;
import com.briup.pai.entity.vo.AssignPermissionVO;
import com.briup.pai.entity.vo.PermissionPageVO;
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
public class PermissionConvertImpl implements PermissionConvert {

    @Override
    public PermissionPageVO po2PermissionPageVO(Permission permission) {
        if ( permission == null ) {
            return null;
        }

        PermissionPageVO permissionPageVO = new PermissionPageVO();

        permissionPageVO.setId( permission.getId() );
        permissionPageVO.setName( permission.getName() );
        permissionPageVO.setPath( permission.getPath() );
        permissionPageVO.setComponent( permission.getComponent() );
        permissionPageVO.setPerm( permission.getPerm() );
        permissionPageVO.setTitle( permission.getTitle() );
        permissionPageVO.setIcon( permission.getIcon() );

        permissionPageVO.setType( getPermissionType(permission.getType()) );
        permissionPageVO.setHidden( getPermissionHidden(permission.getHidden()) );

        return permissionPageVO;
    }

    @Override
    public List<PermissionPageVO> po2PermissionPageVOList(List<Permission> permissions) {
        if ( permissions == null ) {
            return null;
        }

        List<PermissionPageVO> list = new ArrayList<PermissionPageVO>( permissions.size() );
        for ( Permission permission : permissions ) {
            list.add( po2PermissionPageVO( permission ) );
        }

        return list;
    }

    @Override
    public AssignPermissionVO po2AssignPermissionVO(Permission permission) {
        if ( permission == null ) {
            return null;
        }

        AssignPermissionVO assignPermissionVO = new AssignPermissionVO();

        assignPermissionVO.setPermissionId( permission.getId() );
        assignPermissionVO.setPermissionName( permission.getName() );

        return assignPermissionVO;
    }

    @Override
    public List<AssignPermissionVO> po2AssignPermissionVOList(List<Permission> list) {
        if ( list == null ) {
            return null;
        }

        List<AssignPermissionVO> list1 = new ArrayList<AssignPermissionVO>( list.size() );
        for ( Permission permission : list ) {
            list1.add( po2AssignPermissionVO( permission ) );
        }

        return list1;
    }
}
