package com.vanse.jsr303.anno;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
// @Constraint用于指定校验所在的类
@Constraint(validatedBy = GenderValidator.class)
public @interface Gender {
    
    String message() default "Invalid gender value";
    
    Class<?>[] groups() default {};
    
    Class<? extends Payload>[] payload() default {};
}