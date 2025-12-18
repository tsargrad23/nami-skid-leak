package me.lordpvp.phantomware.util;

import java.lang.invoke.*;
import java.lang.reflect.Method;
import java.util.function.Consumer;

public final class ReflectionUtils {
    private static final MethodType CONSUMER_TYPE = MethodType.methodType(void.class, Object.class);
    private static final String CONSUMER_NAME = "accept";

    private ReflectionUtils() {
        throw new AssertionError();
    }

    // https://wiki.openjdk.org/display/HotSpot/Method+handles+and+invokedynamic
    public static Consumer<Object> wrapConsumer(Object obj, Method method) throws Throwable {
        Class<?> klass = obj.getClass();
        MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(klass, MethodHandles.lookup());
        MethodHandle methodHandle = lookup.unreflect(method);
        MethodType target = MethodType.methodType(void.class, method.getParameters()[0].getType());
        MethodType factory = MethodType.methodType(Consumer.class, klass);
        CallSite callSite = LambdaMetafactory.metafactory(lookup, CONSUMER_NAME, factory,
                CONSUMER_TYPE, methodHandle, target);
        MethodHandle handle = callSite.getTarget();
        return (Consumer<Object>) handle.invoke(obj);
    }
}
