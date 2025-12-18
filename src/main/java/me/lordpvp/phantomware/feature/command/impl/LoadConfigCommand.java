package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class LoadConfigCommand extends Command {

    public LoadConfigCommand() {
        super(
                "loadconfig",
                new CommandArgument[] {
                        new CommandArgument.ConfigNameArg("configName")
                },
                "loadcfg", "lcfg"
        );
    }

    @Override
    public void execute(Object[] args) {
        String configName = (String) args[0];

        try {
            CONFIG_MANAGER.loadConfig(configName);
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Config {g}" + configName + "{reset} has been loaded."));
        } catch (Exception e) {
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Failed to load config {g}" + configName + "{reset}: {r}" + e.getMessage() + "{reset}."));
        }
    }
}