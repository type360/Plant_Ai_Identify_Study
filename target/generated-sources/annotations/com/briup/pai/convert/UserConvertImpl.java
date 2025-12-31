package com.briup.pai.convert;

import com.briup.pai.entity.dto.UserSaveDTO;
import com.briup.pai.entity.po.User;
import com.briup.pai.entity.vo.CurrentLoginUserVO;
import com.briup.pai.entity.vo.UserEchoVO;
import com.briup.pai.entity.vo.UserPageVO;
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
public class UserConvertImpl implements UserConvert {

    @Override
    public CurrentLoginUserVO po2CurrentLoginUserVO(User user) {
        if ( user == null ) {
            return null;
        }

        CurrentLoginUserVO currentLoginUserVO = new CurrentLoginUserVO();

        currentLoginUserVO.setUserId( user.getId() );
        currentLoginUserVO.setUsername( user.getUsername() );
        currentLoginUserVO.setHeaderUrl( user.getHeaderUrl() );

        return currentLoginUserVO;
    }

    @Override
    public UserPageVO po2UserPageVO(User user) {
        if ( user == null ) {
            return null;
        }

        UserPageVO userPageVO = new UserPageVO();

        userPageVO.setUserId( user.getId() );
        userPageVO.setUsername( user.getUsername() );
        userPageVO.setRealName( user.getRealName() );
        userPageVO.setTelephone( user.getTelephone() );
        userPageVO.setHeaderUrl( user.getHeaderUrl() );
        userPageVO.setCreateTime( user.getCreateTime() );
        userPageVO.setStatus( user.getStatus() );

        return userPageVO;
    }

    @Override
    public List<UserPageVO> po2UserPageVOList(List<User> users) {
        if ( users == null ) {
            return null;
        }

        List<UserPageVO> list = new ArrayList<UserPageVO>( users.size() );
        for ( User user : users ) {
            list.add( po2UserPageVO( user ) );
        }

        return list;
    }

    @Override
    public User userSaveDTO2Po(UserSaveDTO dto) {
        if ( dto == null ) {
            return null;
        }

        User user = new User();

        user.setId( dto.getUserId() );
        user.setUsername( dto.getUsername() );
        user.setRealName( dto.getRealName() );
        user.setTelephone( dto.getTelephone() );
        user.setHeaderUrl( dto.getHeaderUrl() );

        return user;
    }

    @Override
    public UserEchoVO po2UserEchoVO(User user) {
        if ( user == null ) {
            return null;
        }

        UserEchoVO userEchoVO = new UserEchoVO();

        userEchoVO.setUserId( user.getId() );
        userEchoVO.setUsername( user.getUsername() );
        userEchoVO.setRealName( user.getRealName() );
        userEchoVO.setTelephone( user.getTelephone() );
        userEchoVO.setHeaderUrl( user.getHeaderUrl() );

        return userEchoVO;
    }
}
