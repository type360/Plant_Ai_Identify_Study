package com.vanse.mapstruct;

import com.vanse.mapstruct.entry.dto.UserDTO;
import com.vanse.mapstruct.entry.po.User;
import com.vanse.mapstruct.map.UserConvert;
import org.junit.jupiter.api.Test;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class MapstructApplicationTests {

    @Test
    void contextLoads() {
        // 使用spring的工具转化对象
        UserDTO userDTO = new UserDTO(1,"tom","123",20);
//        User u = new User();
//        BeanUtils.copyProperties(userDTO, u);
//        System.out.println("u = " + u);
//        u.setId(userDTO.getUserId());
    }

    @Test
    void mapStructTest() {
        UserDTO userDTO = new UserDTO(1,"tom","123",20);
        UserConvert convert = UserConvert.userConvert;
        User user = convert.dtoToUser(userDTO);
        System.out.println("user = " + user);


    }

}
