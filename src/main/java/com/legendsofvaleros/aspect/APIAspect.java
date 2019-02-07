package com.legendsofvaleros.aspect;

import com.legendsofvaleros.api.RPCFunction;
import com.legendsofvaleros.api.annotation.ModuleRPC;
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
    public Object wrapRPC(ProceedingJoinPoint joinPoint) throws Throwable {
        if(joinPoint.getArgs().length > 1)
            throw new IllegalArgumentException("Too many arguments!");

        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();

        Module module = Modules.getModule(joinPoint.getThis().getClass());

        RPCFunction<Object> func = new RPCFunction<>(module.getScheduler()::async, method);

        return func.callInternal(method, (joinPoint.getArgs().length == 1 ? joinPoint.getArgs()[0] : null));
    }
}