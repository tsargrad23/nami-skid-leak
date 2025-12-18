package me.lordpvp.phantomware.util;

import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ClasspathScanner {

    @SuppressWarnings("unchecked")
    public static <T> Set<Class<? extends T>> findAnnotated(Class<T> baseClass, Class<?> annotation) {
        Set<Class<? extends T>> result = new HashSet<>();

        try {
            // Always scan within our own package. Scanning the entire classpath is slow and error-prone.
            String resourcePath = "me/kiriyaga/nami";
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(resourcePath);
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    File dir = new File(url.toURI());
                    if (dir.exists() && dir.isDirectory()) {
                        result.addAll(scanDirectory(dir, "me.kiriyaga.nami", baseClass, annotation));
                    }
                } else if ("jar".equals(protocol)) {
                    String path = url.getPath();
                    int bangIndex = path.indexOf("!");
                    if (bangIndex != -1) {
                        String jarFile = path.substring(0, bangIndex);
                        if (jarFile.startsWith("file:")) {
                            jarFile = new File(new URL(jarFile).toURI()).getAbsolutePath();
                        }
                        try (JarFile jar = new JarFile(jarFile)) {
                            result.addAll(scanJar(jar, baseClass, annotation));
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    private static <T> Set<Class<? extends T>> scanDirectory(File dir, String pkg, Class<T> base, Class<?> annotation) throws ClassNotFoundException {
        Set<Class<? extends T>> result = new HashSet<>();

        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.isDirectory()) {
                result.addAll(scanDirectory(file, pkg + "." + file.getName(), base, annotation));
            } else if (file.getName().endsWith(".class")) {
                String className = pkg + "." + file.getName().replace(".class", "");

                if (file.getName().equals("module-info.class")) {
                    continue;
                }

                if (className.startsWith("me.kiriyaga.nami.mixin.")) {
                    continue;
                }

                Class<?> cls = Class.forName(className);

                if (base.isAssignableFrom(cls) && !cls.isInterface() && !Modifier.isAbstract(cls.getModifiers()) && cls.isAnnotationPresent((Class<? extends Annotation>) annotation)) {
                    result.add((Class<? extends T>) cls);
                }
            }
        }

        return result;
    }

    private static <T> Set<Class<? extends T>> scanJar(JarFile jar, Class<T> base, Class<?> annotation) throws ClassNotFoundException {
        Set<Class<? extends T>> result = new HashSet<>();
        Enumeration<JarEntry> entries = jar.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();

            if (name.endsWith(".class")) {
                if (name.equals("module-info.class")) {
                    continue;
                }

                if (name.startsWith("META-INF/versions/")) {
                    continue;
                }

                if (name.startsWith("me/kiriyaga/nami/mixin/")) {
                    continue;
                }

                String className = name.replace('/', '.').replace(".class", "");

                if (!className.startsWith("me.kiriyaga.nami.")) {
                    continue;
                }

                Class<?> cls = Class.forName(className, false, Thread.currentThread().getContextClassLoader());

                if (base.isAssignableFrom(cls) && !cls.isInterface() && !Modifier.isAbstract(cls.getModifiers()) && cls.isAnnotationPresent((Class<? extends Annotation>) annotation)) {
                    result.add((Class<? extends T>) cls);
                }
            }
        }

        return result;
    }
}
