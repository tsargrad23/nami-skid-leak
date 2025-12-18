package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class ListConfigCommand extends Command {

    public ListConfigCommand() {
        super("listconfig",
                new CommandArgument[] {},
                "configlist", "lc");
    }

    @Override
    public void execute(Object[] args) {
        try {
            var configs = CONFIG_MANAGER.getConfigSerializer().listConfigs();
            if (configs.isEmpty()) {
                CHAT_MANAGER.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format("No configs found."));
            } else {
                StringBuilder builder = new StringBuilder("Configs: ");
                for (int i = 0; i < configs.size(); i++) {
                    builder.append("{g}").append(configs.get(i)).append("{reset}");
                    if (i < configs.size() - 1) builder.append(", ");
                }
                CHAT_MANAGER.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format(builder.toString()));
            }
        } catch (Exception e) {
            CHAT_MANAGER.sendPersistent(getClass().getName(),
                    CAT_FORMAT.format("Failed to list configs: {g}" + e + "{reset}."));
        }
    }
}
