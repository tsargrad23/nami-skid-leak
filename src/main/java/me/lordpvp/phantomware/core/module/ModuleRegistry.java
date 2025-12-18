package me.kiriyaga.nami.core.module;

import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.util.ClasspathScanner;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModuleRegistry {

        public static void registerAnnotatedModules(ModuleStorage storage) {
            Set<Class<? extends Module>> classes = ClasspathScanner.findAnnotated(Module.class, RegisterModule.class);
            for (Class<? extends Module> clazz : classes) {
                try {
                    Module module = clazz.getDeclaredConstructor().newInstance();
                    storage.add(module);
                } catch (Exception e) {
                    System.err.println("Failed to instantiate module: " + clazz.getName());
                    e.printStackTrace();
                }
            }
        }
}
