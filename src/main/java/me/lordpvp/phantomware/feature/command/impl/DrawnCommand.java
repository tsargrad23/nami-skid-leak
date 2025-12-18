package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import me.kiriyaga.nami.feature.module.Module;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class DrawnCommand extends Command {

    public DrawnCommand() {
        super("drawn",
                new CommandArgument[] {
                        new CommandArgument.ModuleArg("moduleName")
                },
                "draw", "drawmodule", "moduledraw");
    }

    @Override
    public void execute(Object[] args) {
        String input = args[0].toString();

        Module found = null;
        for (Module m : MODULE_MANAGER.getStorage().getAll()) {
            if (m.matches(input)) {
                found = m;
                break;
            }
        }

        if (found == null) {
            CHAT_MANAGER.sendTransient(
                    CAT_FORMAT.format("Module {g}" + input + "{reset} not found."));
            return;
        }

        found.setDrawn(!found.isDrawn());
        CHAT_MANAGER.sendTransient(
                CAT_FORMAT.format("Module {g}" + input + " {reset}drawn changed."));
    }
}