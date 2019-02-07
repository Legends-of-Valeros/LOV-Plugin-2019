package com.legendsofvaleros.api;

import com.legendsofvaleros.api.annotation.ModuleRPC;

public interface APITest {
    Promise<Object> testRPC();
    Promise<Object> ping();

    @ModuleRPC("ping")
    Object pingSync();
}