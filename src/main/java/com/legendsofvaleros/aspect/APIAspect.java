package com.legendsofvaleros.aspect;

import com.legendsofvaleros.api.RPCFunction;
import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.Modules;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
public class APIAspect {
    @Around("@annotation(com.legendsofvaleros.api.annotation.ModuleRPC) && execution(* *(..))")
    public Object wrapRPC(ProceedingJoinPoint joinPoint) {
        Module module = Modules.getModule(joinPoint.getThis().getClass());
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        return RPCFunction.callMethod(module.getScheduler()::async, method, joinPoint.getArgs());
    }
}