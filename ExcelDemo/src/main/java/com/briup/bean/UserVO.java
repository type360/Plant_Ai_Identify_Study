package com.briup.bean;

import com.alibaba.excel.annotation.ExcelIgnore;
import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.DateTimeFormat;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import com.alibaba.excel.annotation.write.style.ContentRowHeight;
import com.alibaba.excel.annotation.write.style.HeadRowHeight;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.briup.utils.GenderConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
//内容行高
@ContentRowHeight(15)
//表头行高
@HeadRowHeight(20)
public class UserVO implements Serializable {

	@ExcelIgnore
	private Integer id;

	@ExcelProperty(value = {"用户数据","姓名"}, index = 0)
	private String name;

	@ExcelProperty(value = {"用户数据","年龄"}, index = 1)
	private Integer age;

	@ExcelProperty(value = {"用户数据","性别"}, index = 2, converter = GenderConverter.class)
	private Integer gender;

	@ExcelProperty(value = {"用户数据","地址"}, index = 3)
	private String address;

	@ColumnWidth(30)
	@DateTimeFormat("yyyy年MM月dd日HH时mm分ss秒")
	@ExcelProperty(value = "创建时间", index = 5)
	private LocalDateTime createTime;

	@NumberFormat("0.00")
	@ExcelProperty(value = {"用户数据","工资"}, index = 4)
	private Double salary;
}