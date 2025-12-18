package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.core.executable.model.ExecutableThreadType;
import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.module.Module;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class ToggleCommand extends Command {

    public ToggleCommand() {
        super("toggle",
                new CommandArgument[] {
                        new CommandArgument.ModuleArg("name")
                },
                "on", "off", "switch", "togle", "turnon", "turnoff", "tggle");
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

        EXECUTABLE_MANAGER.getRequestHandler().submit(found::toggle, 2, ExecutableThreadType.PRE_TICK);
    }
}