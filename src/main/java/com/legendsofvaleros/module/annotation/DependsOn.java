package com.legendsofvaleros.module.annotation;

import com.legendsofvaleros.module.Module;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(Dependencies.class)
public @interface DependsOn {
    Class<? extends Module> value();
    boolean optional() default false;
}
