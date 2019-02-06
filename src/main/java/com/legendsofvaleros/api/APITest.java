package com.legendsofvaleros.api;

import com.google.common.util.concurrent.ListenableFuture;
import com.legendsofvaleros.api.annotation.ModuleRPC;

@ModuleRPC
public interface APITest {
    ListenableFuture<Object> testRPC();
    ListenableFuture<Object> ping();
    Object pingSync();
}