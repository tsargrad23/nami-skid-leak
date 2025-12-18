package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import me.kiriyaga.nami.mixininterface.ISimpleOption;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class FovCommand extends Command {

    public FovCommand() {
        super(
                "fov",
                new CommandArgument[]{
                        new CommandArgument.IntArg("value", 0, 162)
                },
                "fav", "fv"
        );
    }

    @Override
    public void execute(Object[] args) {
        int newFov = (int) args[0];

        ((ISimpleOption)(Object) MC.options.getFov()).setValue(newFov);

        CHAT_MANAGER.sendPersistent(FovCommand.class.getName(),
                CAT_FORMAT.format("FOV set to: {g}" + newFov + "{reset}."));
    }
}