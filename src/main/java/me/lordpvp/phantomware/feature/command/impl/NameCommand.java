package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class NameCommand extends Command {

    public NameCommand() {
        super(
                "name",
                new CommandArgument[] {
                        new CommandArgument.StringArg("name", 1, 24)
                },
                "n", "nam", "mne", "nome", "brand", "changename"
        );
    }

    @Override
    public void execute(Object[] args) {
        String newName = (String) args[0];

        DISPLAY_NAME = newName;
        CONFIG_MANAGER.saveName(newName);

        CHAT_MANAGER.sendPersistent(NameCommand.class.getName(),
                CAT_FORMAT.format("Name set to: {g}" + newName + "{reset}."));
    }
}