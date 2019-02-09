package com.legendsofvaleros.api;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.legendsofvaleros.api.annotation.ModuleRPC;
import io.deepstream.RpcResult;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.Executor;

public class RPCFunction<T> {
    private final Executor executor;
    private final String func;
    private final Class<T> result;

    public RPCFunction(Executor executor, String func, Class<T> result) {
        this.executor = executor != null ? executor : APIController.getInstance().getPool();
        this.func = func;
        this.result = result;
    }

    public static RPCFunction create(Executor exec, Method m) {
        return new RPCFunction(exec, getMethodName(m), getMethodResultType(m));
    }

    public Promise<T> call(Object... args) {
        return this.oneShotAsync(this.executor, this.func, this.result, args);
    }

    public T callSync(Object... args) {
        return this.oneShotSync(this.func, this.result, args);
    }

    /*
    * For RPC methods proxied or created on demand, there's no point to create an object.
    * These static functions are used if you have no way to store the RPCFunction object.
    * */

    public static <T> Promise<T> oneShotAsync(Executor exec, String func, Class<T> result, Object... args) {
        return Promise.make(() -> oneShotSync(func, result, args), exec);
    }

    public static <T> T oneShotSync(String func, Class<T> result, Object... args) {
        try {
            Object arg = null;
            if (args.length == 1) arg = args[0];
            else if (args.length > 1) arg = args;

            // This is a hack so we can use our own Gson parser.
            // TODO: Fix when deepstream supports passing our own data.
            Gson gson = APIController.getInstance().getGson();
            if (gson == null)
                throw new IllegalStateException("Must wait until postLoad() before using RPC functions!");

            RpcResult res = APIController.getInstance().getClient().rpc.make(func,
                    arg != null ? gson.fromJson(gson.toJson(arg), JsonElement.class) : null);

            if (res.success()) {
                // Decode result into T using Gson
                if (res.getData() instanceof JsonElement)
                    return APIController.getInstance().getGson().fromJson((JsonElement) res.getData(), result);

                if (res.getData() instanceof String)
                    return APIController.getInstance().getGson().fromJson((String) res.getData(), result);

                return (T) res.getData();
            }

            throw new RuntimeException("" + res.getData());
        } catch(Exception e) {
            throw new RuntimeException(func + "() -> " + result.getSimpleName() + " failure!", e);
        }
    }

    public static Object callMethod(Executor ifAsync, Method m, Object... args) {
        if(Promise.class.isAssignableFrom(m.getReturnType()))
            return oneShotAsync(ifAsync, getMethodName(m), getMethodResultType(m), args);
        return oneShotSync(getMethodName(m), m.getReturnType(), args);
    }

    /**
     * Gets the real RPC method to call from the method name and containing class.
     */
    public static String getMethodName(Method m) {
        StringBuilder name = new StringBuilder();

        Class<?> c = m.getDeclaringClass();

        if(c.getAnnotation(ModuleRPC.class) != null) {
            name.append(c.getAnnotation(ModuleRPC.class).value());
            if (name.length() > 0) name.append(":");
        }

        if(m.getAnnotation(ModuleRPC.class) != null)
            name.append(m.getAnnotation(ModuleRPC.class).value());
        else
            name.append(m.getName());

        return name.toString();
    }

    /**
     * Get the current decode class type from the method.
     *
     * For example: Float from Promise<Float>
     */
    public static Class<?> getMethodResultType(Method m) {
        Class<?> resultType = m.getReturnType();
        if(Promise.class.isAssignableFrom(resultType)) {
            Type type = ((ParameterizedType)m.getGenericReturnType()).getActualTypeArguments()[0];

            if(type instanceof ParameterizedTypeImpl)
                resultType = ((ParameterizedTypeImpl)type).getRawType();
            else
                resultType = (Class<?>)type;
        }
        return resultType;
    }
}
