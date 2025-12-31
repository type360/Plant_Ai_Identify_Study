package com.vanse.jsr303.controller;

import com.vanse.jsr303.anno.ValidateGroups;
import com.vanse.jsr303.dto.StudentAddDTO1;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Auther: vanse(lc)
 * @Date: 2025/10/30-10-30-下午2:08
 * @Description：
 */
@RestController
@RequestMapping("student")
@Validated
public class StudentController {
    @PostMapping("add")
    public String student(@Validated(ValidateGroups.update.class) @RequestBody StudentAddDTO1 stu){
        System.out.println("stu = " + stu);
        return "添加成功";
    }

    @DeleteMapping("delete")
    public String removeStudentByIds(@RequestBody @Size(min = 1, message = "学生id列表不能为空") List<Integer> ids) {
        // 具体删除学生的业务……
        System.out.println(ids);
        return "remove student success";
    }
}









