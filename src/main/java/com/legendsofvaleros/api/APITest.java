package com.legendsofvaleros.api;

import com.legendsofvaleros.api.annotation.ModuleRPC;

import java.util.UUID;

public interface APITest {
    Promise<UUID> find(UUID id);
    Promise<Float> ping();

    @ModuleRPC("ping")
    Float pingSync();
}