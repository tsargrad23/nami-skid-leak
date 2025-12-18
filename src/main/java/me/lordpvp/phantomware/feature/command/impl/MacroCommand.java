package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.core.macro.model.Macro;
import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import me.kiriyaga.nami.util.KeyUtils;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class MacroCommand extends Command {

    public MacroCommand() {
        super(
                "macro",
                new CommandArgument[]{
                        new CommandArgument.ActionArg("add/del/list", "add", "del", "list"),
                        new CommandArgument.StringArg("key", 1, 16){
                            @Override
                            public boolean isRequired() {
                                return false;
                            }
                        },
                        new CommandArgument.StringArg("message", 1, 256) {
                            @Override
                            public boolean isRequired() {
                                return false;
                            }

                            @Override
                            public Object parse(String[] input, int index) {
                                StringBuilder builder = new StringBuilder();
                                for (int i = index; i < input.length; i++) {
                                    builder.append(input[i]);
                                    if (i != input.length - 1) builder.append(" ");
                                }
                                return builder.toString();
                            }
                        }
                },
                "mac", "m"
        );
    }

    @Override
    public void execute(Object[] args) {
        String action = (String) args[0];

        switch (action) {
            case "add" -> {
                String keyName = ((String) args[1]).toUpperCase();
                String message = (String) args[2];

                int keyCode = KeyUtils.parseKey(keyName);
                if (keyCode == -1) {
                    CHAT_MANAGER.sendPersistent(getClass().getName(),
                            CAT_FORMAT.format("Invalid key: {g}" + keyName + "{reset}."));
                    return;
                }

                //MACRO_MANAGER.removeMacro(keyCode);
                MACRO_MANAGER.addMacro(new Macro(keyCode, message));
                CONFIG_MANAGER.saveMacros();

                CHAT_MANAGER.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format("Macro added: {g}" + keyName + " " + message + "{reset}."));
            }

            case "del" -> {
                String keyName = ((String) args[1]).toUpperCase();
                int keyCode = KeyUtils.parseKey(keyName);
                if (keyCode == -1) {
                    CHAT_MANAGER.sendPersistent(getClass().getName(),
                            CAT_FORMAT.format("Invalid key: {g}" + keyName + "{reset}."));
                    return;
                }

                MACRO_MANAGER.removeMacro(keyCode);
                CONFIG_MANAGER.saveMacros();

                CHAT_MANAGER.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format("Macro removed: {g}" + keyName + "{reset}."));
            }

            case "list" -> {
                if (MACRO_MANAGER.getAll().isEmpty()) {
                    CHAT_MANAGER.sendPersistent(getClass().getName(),
                            CAT_FORMAT.format("No macros have been added."));
                    return;
                }

                CHAT_MANAGER.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format("Macros:"));
                for (Macro macro : MACRO_MANAGER.getAll()) {
                    String key = KeyUtils.getKeyName(macro.getKeyCode());
                    String msg = macro.getMessage();
                    CHAT_MANAGER.sendPersistent(getClass().getName(),
                            CAT_FORMAT.format("  {g}" + key + " " + msg + "{reset},"));
                }
            }

            default -> {
                CHAT_MANAGER.sendPersistent(getClass().getName(),
                        CAT_FORMAT.format("Unknown action: {g}" + action + "{reset}."));
            }
        }
    }
}
