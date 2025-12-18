package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import me.kiriyaga.nami.mixininterface.ISimpleOption;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class GammaCommand extends Command {

    public GammaCommand() {
        super(
                "gamma",
                new CommandArgument[]{
                        new CommandArgument.IntArg("value", 0, 420)
                },
                "light", "brightens", "bright"
        );
    }

    @Override
    public void execute(Object[] args) {
        int newGamma = (int) args[0];

        ((ISimpleOption) (Object) MC.options.getGamma()).setValue((double) newGamma);
        CHAT_MANAGER.sendPersistent(GammaCommand.class.getName(),
                CAT_FORMAT.format("Gamma set to: {g}" + newGamma + "{reset}."));
    }
}