package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class SaveCommand extends Command {

    public SaveCommand() {
        super("save",
                new CommandArgument[0], "s", "save", "seva", "sv");
    }

    @Override
    public void execute(Object[] args) {
        try {
            CONFIG_MANAGER.saveModules();
            CHAT_MANAGER.sendPersistent(SaveCommand.class.getName(),
                    CAT_FORMAT.format("Config has been saved."));
        } catch (Exception e) {
            CHAT_MANAGER.sendPersistent(SaveCommand.class.getName(),
                    CAT_FORMAT.format("Config has not been saved: {g}" + e + "{reset}."));
        }
    }
}
