package com.vanse.mapstruct.map;

import com.vanse.mapstruct.entry.dto.UserDTO;
import com.vanse.mapstruct.entry.po.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * @Auther: vanse(lc)
 * @Date: 2025/10/30-10-30-下午4:03
 * @Description：通过Mappers的静态方法动态获取出对象
 */
@Mapper
public interface UserConvert {
    public static final UserConvert userConvert =
               Mappers.getMapper(UserConvert.class);
    // 对象调用普通方法
    // UserDto -> User
    // source是参数中的属性 target是返回值的属性
    @Mapping(target = "id", source = "userId")
    @Mapping(target = "age", source = "userAge")
    public abstract User dtoToUser(UserDTO dto);
}
