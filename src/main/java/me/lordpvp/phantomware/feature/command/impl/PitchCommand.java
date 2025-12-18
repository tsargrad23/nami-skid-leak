package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class PitchCommand extends Command {

    public PitchCommand() {
        super(
                "pitch",
                new CommandArgument[] {
                        new CommandArgument.IntArg("value", -90, 90)
                },
                "p"
        );
    }

    @Override
    public void execute(Object[] args) {
        int pitch = (int) args[0];

        if (MC.player != null) {
            MC.player.setPitch(pitch);
            CHAT_MANAGER.sendPersistent(PitchCommand.class.getName(),
                    CAT_FORMAT.format("Pitch set to: {g}" + pitch + "{reset}."));
        }
    }
}
