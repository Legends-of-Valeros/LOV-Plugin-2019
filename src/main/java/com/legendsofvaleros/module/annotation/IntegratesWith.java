package com.legendsofvaleros.module.annotation;

import com.legendsofvaleros.module.Integration;
import com.legendsofvaleros.module.Module;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD })
@Repeatable(Integrations.class)
public @interface IntegratesWith {
    Class<? extends Module> module();
    Class<? extends Integration> integration() default Integration.class;
}