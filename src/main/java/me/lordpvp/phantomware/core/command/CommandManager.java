package me.kiriyaga.nami.core.command;

import static me.kiriyaga.nami.Nami.*;

public class CommandManager {

    private final CommandStorage storage = new CommandStorage();
    private final CommandExecutor executor = new CommandExecutor(storage);
    private final CommandSuggester suggester = new CommandSuggester(storage);

    public void init() {
        CommandRegistry.registerAnnotatedCommands(storage);
        CommandRegistry.registerModuleCommands(storage);
        suggester.updateDispatcher();
        EVENT_MANAGER.register(executor);
        LOGGER.info("Registered " + storage.size() + " commands.");
    }

    public CommandStorage getStorage() {
        return storage;
    }

    public CommandExecutor getExecutor() {
        return executor;
    }

    public CommandSuggester getSuggester() {
        return suggester;
    }

    public void addCommand(me.kiriyaga.nami.feature.command.Command command) {
        storage.addCommand(command);
        suggester.updateDispatcher();
    }

    public void removeCommand(me.kiriyaga.nami.feature.command.Command command) {
        storage.removeCommand(command);
        suggester.updateDispatcher();
    }

}
