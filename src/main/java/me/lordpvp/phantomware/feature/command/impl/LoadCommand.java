package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class LoadCommand extends Command {

    public LoadCommand() {
        super("load",
                new CommandArgument[] {},
                "l", "laod", "lad", "lod");
    }

    @Override
    public void execute(Object[] args) {
        try {
            CONFIG_MANAGER.loadModules();
            CHAT_MANAGER.sendPersistent(LoadCommand.class.getName(),
                    CAT_FORMAT.format("Config has been loaded."));
        } catch (Exception e) {
            CHAT_MANAGER.sendPersistent(LoadCommand.class.getName(),
                    CAT_FORMAT.format("Config has not been loaded: {g}" + e.getMessage() + "{reset}."));
        }
    }
}
