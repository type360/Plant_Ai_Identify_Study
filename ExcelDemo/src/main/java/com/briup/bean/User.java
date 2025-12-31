package com.briup.bean;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentStyle;
import com.alibaba.excel.enums.poi.HorizontalAlignmentEnum;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.briup.utils.GenderConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tb_user")
public class User implements Serializable {

    /**
    * 用户id
    */
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ExcelIgnore
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /**
    * 用户名
    */
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ExcelProperty(value = {"用户数据","姓名"},index = 0)
    //@ExcelProperty
    private String name;
    /**
    * 年龄
    */
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ExcelProperty(value = {"用户数据","年龄"},index = 1)
    private Integer age;
    /**
    * 性别:0为女性,1为男性
    */
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ExcelProperty(value = {"用户数据","性别"},index = 2,converter = GenderConverter.class)
    private Integer gender;
    /**
    * 地址
    */
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ExcelProperty(value = {"用户数据","地址"},index = 5)
    private String address;
    /**
    * 创建时间
    */
    @ColumnWidth(25)
    @ExcelProperty(value = "创建时间", index = 5)
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @TableField("create_time")
    @DateTimeFormat("yyyy年MM月dd日HH时mm分ss秒")
    private LocalDateTime createTime;
    /**
    * 工资
    */
    @ContentStyle(horizontalAlignment = HorizontalAlignmentEnum.CENTER)
    @ExcelProperty(value = {"用户数据","工资"},index = 4)
    @NumberFormat("0.00")
    private Double salary;

}
