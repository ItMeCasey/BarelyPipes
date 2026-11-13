package com.itmecasey.barelypipes.core.script;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.logging.Logger;

/**
 * Gives ModJS scripts a global {@code BarelyPipes} (the {@link PipeScripts}). ModJS is optional, so everything here goes through
 * reflection: ModJS offers {@code ExtraBindings.register(BindingProvider)} and the provider's {@code install(type, cx, scope)} adds
 * globals to every script scope. Without ModJS nothing is loaded and nothing fails. Client scripts get no global, because the
 * networks live on the server.
 */
public final class ModJsBridge {
    private static final Logger LOG = Logger.getLogger("barelypipes");

    private ModJsBridge() {
    }

    /** Installs the binding when ModJS is on the classpath; returns whether it did. */
    public static boolean install(PipeScripts api) {
        try {
            Class<?> provider = Class.forName("com.itmecasey.modjs.script.BindingProvider");
            Class<?> extra = Class.forName("com.itmecasey.modjs.script.ExtraBindings");
            Class<?> context = Class.forName("org.mozilla.javascript.Context");
            Class<?> scriptable = Class.forName("org.mozilla.javascript.Scriptable");
            Class<?> scriptableObject = Class.forName("org.mozilla.javascript.ScriptableObject");
            Method javaToJs = context.getMethod("javaToJS", Object.class, scriptable);
            Method put = scriptableObject.getMethod("putProperty", scriptable, String.class, Object.class);
            InvocationHandler handler = (proxy, method, args) -> {
                if (method.getName().equals("install") && args != null && args.length == 3) {
                    if (args[0] != null && "CLIENT".equals(args[0].toString())) {
                        return null;
                    }
                    put.invoke(null, args[2], "BarelyPipes", javaToJs.invoke(null, api, args[2]));
                    return null;
                }
                if (method.getName().equals("toString")) {
                    return "BarelyPipes binding";
                }
                if (method.getName().equals("hashCode")) {
                    return System.identityHashCode(proxy);
                }
                if (method.getName().equals("equals")) {
                    return proxy == args[0];
                }
                return null;
            };
            Object binding = Proxy.newProxyInstance(provider.getClassLoader(), new Class<?>[] {provider}, handler);
            extra.getMethod("register", provider).invoke(null, binding);
            ScriptHub.enable();
            LOG.info("Barely Pipes: ModJS server scripts can use the BarelyPipes global");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            LOG.warning("Barely Pipes: could not add the BarelyPipes global to ModJS: " + e);
            return false;
        }
    }
}
