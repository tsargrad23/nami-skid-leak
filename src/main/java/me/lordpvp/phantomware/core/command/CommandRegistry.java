package me.kiriyaga.nami.core.command;

import me.kiriyaga.nami.feature.command.impl.ModuleCommand;
import me.kiriyaga.nami.util.ClasspathScanner;
import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import java.util.Set;

import static me.kiriyaga.nami.Nami.*;

public class CommandRegistry {

    public static void registerAnnotatedCommands(CommandStorage storage) {
        Set<Class<? extends Command>> classes = ClasspathScanner.findAnnotated(Command.class, RegisterCommand.class);

        for (Class<? extends Command> clazz : classes) {
            try {
                Command command = clazz.getDeclaredConstructor().newInstance();
                storage.addCommand(command);
            } catch (Exception e) {
                LOGGER.error("Failed to instantiate command: " + clazz.getName(), e);
            }
        }
    }

    public static void registerModuleCommands(CommandStorage storage) {
        MODULE_MANAGER.getStorage().getAll().forEach(module -> {
            try {
                String name = module.getName().replace(" ", "");
                if (storage.getCommandByNameOrAlias(name) == null) {
                    storage.addCommand(new ModuleCommand(module));
                }
            } catch (Exception e) {
                LOGGER.error("Failed to initiate command for module: " + module.getName(), e);
            }
        });
    }
}