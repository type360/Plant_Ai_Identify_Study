package com.vanse.jsr303.dto;

import com.vanse.jsr303.anno.Gender;
import com.vanse.jsr303.anno.ValidateGroups;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.Data;
import org.hibernate.validator.constraints.Range;

/**
 * dto 数据传递包 接收参数用[注解] swagger注解
 * po 实体包 一个数据库 -> 一个实体  实体和数据库一样
 * vo 视图包 响应数据用
 */
@Data
public class StudentAddDTO1 {
    @NotNull(message = "id不能为空", groups = ValidateGroups.update.class)
    @Null(message = "id必须为空", groups = ValidateGroups.insert.class)
    private int id;
    @NotBlank(message = "姓名不能为空或者空串")
    private String name;

    @NotNull(message = "年龄不能为空")
    @Range(min = 1, max = 30, message = "学生年龄必须在1-30之间")
    private Integer age;

    // 也可以用正则表达式 "$\w[]^"
    @Gender(message = "性别只能是男或者女")
    private String gender;
}