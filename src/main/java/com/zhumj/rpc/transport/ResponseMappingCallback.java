package com.zhumj.rpc.transport;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ResponseMappingCallback {

    private static final ConcurrentHashMap<Long, CompletableFuture> responseCallback = new ConcurrentHashMap<>();


    public static void registerCallback(Long requestId, CompletableFuture future) {
        responseCallback.put(requestId, future);
    }

    public static CompletableFuture getCallback(Long requestId) {
        return responseCallback.get(requestId);
    }

}
