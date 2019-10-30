package com.legendsofvaleros.modules.quests.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * I know what you're thinking "OH STUMBLIN WHY DONT YOU JUST USE @EventHandler FOR QUEST NODE LISTENERS!"
 * <p/>
 * Well, I didn't want there to be conflict between how @EventHandler works within quest nodes and how it works outside
 * of quest nodes. It's better to have defined use cases for each annotation, and overriding how an existing, well known
 * one works, is not smart.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface QuestEvent {
    /**
     * Makes sure to handle the event in an async thread.
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Async {

    }
}