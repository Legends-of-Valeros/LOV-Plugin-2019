package com.legendsofvaleros;

import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.ModuleManager;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.bukkit.event.Event;

@Aspect
public class EventsAspect {
    @Around("@annotation(org.bukkit.event.EventHandler) && execution(* *(..))")
    public Object wrapEvents(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        Event event = (Event)joinPoint.getArgs()[0];
        Module module = ModuleManager.getModule(joinPoint.getThis().getClass());

        Object returnObject;

        try {
            returnObject = joinPoint.proceed();
        } catch(Throwable throwable) {
            throw throwable;
        } finally {
            long tookTime = System.currentTimeMillis() - startTime;

            if(module != null)
                module.getTimings().calledEvent(event, tookTime);
        }

        return returnObject;
    }
}