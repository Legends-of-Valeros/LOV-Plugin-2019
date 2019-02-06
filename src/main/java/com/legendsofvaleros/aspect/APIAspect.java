package com.legendsofvaleros.aspect;

import com.legendsofvaleros.api.RPCFunction;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

@Aspect
public class APIAspect {
    @Around("@annotation(com.legendsofvaleros.api.RPC) && execution(* *(..))")
    public Object wrapRPC(ProceedingJoinPoint joinPoint) throws Throwable {
        if(joinPoint.getArgs().length > 1)
            throw new IllegalArgumentException("Too many arguments!");

        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        Method method = signature.getMethod();

        RPCFunction<Object> func = new RPCFunction<>(method);

        return func.callInternal(method, (joinPoint.getArgs().length == 1 ? joinPoint.getArgs()[0] : null));
    }
}