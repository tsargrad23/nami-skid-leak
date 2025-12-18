package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import me.kiriyaga.nami.feature.command.CommandArgument;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class SaveConfigCommand extends Command {

    public SaveConfigCommand() {
        super("saveconfig",
                new CommandArgument[] {
                        new CommandArgument.ConfigNameArg("configName")
                },
                "savecfg", "scfg");
    }

    @Override
    public void execute(Object[] args) {
        String configName = args[0].toString();

        try {
            CONFIG_MANAGER.saveConfig(configName);
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Config {g}" + configName + "{reset} has been saved."));
        } catch (Exception e) {
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Failed to save config {g}" + configName + "{reset}: {g}" + e + "{reset}."));
        }
    }
}
