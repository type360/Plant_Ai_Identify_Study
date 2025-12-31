package com.briup;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.briup.bean.User;
import com.briup.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

/**
 * @author 86151
 * @program: Plant_Ai_Identify_Study
 * @description
 * @create 2025/12/18 15:33
 **/
@SpringBootTest
public class ExcleWriteTest {
    @Autowired
    private UserService userService;

    @Test
    public void writeFormat() {
        List<User> list = userService.list();
        String filename = "C:\\Users\\86151\\Desktop\\项目小组联络表\\4.xlsx";
        EasyExcel.write(filename,User.class)
                .excelType(ExcelTypeEnum.XLSX)
                .sheet("用户数据")
                .doWrite(list);
    }

}
