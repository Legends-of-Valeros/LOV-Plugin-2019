package com.legendsofvaleros.api;

import com.legendsofvaleros.api.annotation.ModuleRPC;

public interface APITest {
    Promise<Object> testRPC();
    Promise<Float> ping();

    @ModuleRPC("ping")
    Float pingSync();
}