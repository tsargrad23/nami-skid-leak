package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class YawCommand extends Command {

    public YawCommand() {
        super(
                "yaw",
                new CommandArgument[] {
                        new CommandArgument.DoubleArg("value", -180, 180)
                },
                "y"
        );
    }

    @Override
    public void execute(Object[] parsedArgs) {
        double yawDouble = (double) parsedArgs[0];
        float yaw = (float) yawDouble;

        MC.player.setYaw(yaw);
        CHAT_MANAGER.sendPersistent(getClass().getName(),
                CAT_FORMAT.format("Yaw set to: {g}" + yaw + "{reset}."));
    }
}