package com.lyd.utils;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author 天狗
 * @desc: 接口防止多次访问注解类
 * @date 2022/7/18
 */

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AccessLimitAnno {

    int seconds();
    int maxcount();
    boolean needLogin() default true;

}
