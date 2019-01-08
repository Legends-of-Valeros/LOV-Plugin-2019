package com.legendsofvaleros.aspect;

import com.legendsofvaleros.module.Module;
import com.legendsofvaleros.module.Modules;
import com.legendsofvaleros.util.MessageUtil;
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
        Module module = Modules.getModule(joinPoint.getThis().getClass());

        Object returnObject;

        try {
            returnObject = joinPoint.proceed();
        } catch(Throwable th) {
            MessageUtil.sendSevereException(module, null, th);
            throw th;
        } finally {
            long tookTime = System.currentTimeMillis() - startTime;

            if(module != null)
                module.getTimings().calledEvent(event, tookTime);
        }

        return returnObject;
    }
}