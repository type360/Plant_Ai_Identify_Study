package com.vanse.jsr303.anno;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

public class GenderValidator implements ConstraintValidator<Gender, String> {

    /**
     * 校验方法
     * @param value                       待校验参数值
     * @param constraintValidatorContext  校验上下文
     * @return                            校验结果（true：校验通过，false：校验不通过）
     */
    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        // 参数为空直接返回true
        if (!StringUtils.hasText(value)) {
            return true;
        }
        return "男".equals(value) || "女".equals(value);
    }
}