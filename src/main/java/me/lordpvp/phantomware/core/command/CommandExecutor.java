package me.kiriyaga.nami.core.command;

import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.ChatMessageEvent;
import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;

import static me.kiriyaga.nami.Nami.*;
import static me.kiriyaga.nami.Nami.LOGGER;

import java.util.ArrayList;
import java.util.List;

public class CommandExecutor {

    private final CommandStorage storage;
    private String prefix = "-";

    public CommandExecutor(CommandStorage storage) {
        this.storage = storage;
    }

    public void setPrefix(String prefix) {
        if (prefix != null && !prefix.isEmpty()) {
            this.prefix = prefix;
            LOGGER.info("Command prefix changed to: " + prefix);
        } else {
            LOGGER.warn("Attempted to set empty or null prefix.");
        }
    }

    public String getPrefix() {
        return prefix;
    }

    @SubscribeEvent
    public void onChatMessage(ChatMessageEvent event) {
        String message = event.getMessage();

        if (!message.startsWith(prefix)) return;

        event.setCancelled(true);

        String[] parts = tokenize(message);
        if (parts.length == 0) return;

        String cmdName = parts[0].substring(prefix.length());

        if (cmdName.isEmpty()) {
            CHAT_MANAGER.sendPersistent(CommandExecutor.class.getName(),
                    CAT_FORMAT.format("Please specify a command. Use {g}" + prefix + "help{reset} for a list."));
            return;
        }

        String[] args = new String[parts.length - 1];
        System.arraycopy(parts, 1, args, 0, args.length);

        Command command = storage.getCommandByNameOrAlias(cmdName);
        if (command == null) {
            CHAT_MANAGER.sendPersistent(CommandExecutor.class.getName(),
                    CAT_FORMAT.format("Unknown command: {g}" + cmdName + "{reset}. Use {g}" + prefix + "help{reset}."));
            return;
        }

        CommandArgument[] expected = command.getArguments();
        Object[] parsed = new Object[expected.length];

        try {
            int requiredCount = 0;
            for (CommandArgument arg : expected) {
                if (arg.isRequired()) requiredCount++;
            }
            if (args.length < requiredCount) {
                throw new IllegalArgumentException("Missing required arguments.");
            }

            for (int i = 0; i < expected.length; i++) {
                CommandArgument arg = expected[i];

                if (i >= args.length) {
                    if (!arg.isRequired()) {
                        parsed[i] = null;
                        continue;
                    } else {
                        throw new IllegalArgumentException("Missing argument: " + arg.getName());
                    }
                }

                if (i == expected.length - 1 && arg instanceof CommandArgument.StringArg stringArg) {
                    String remaining = String.join(" ", java.util.Arrays.copyOfRange(args, i, args.length));
                    parsed[i] = stringArg.parse(remaining);
                    break;
                }

                String input = args[i];
                parsed[i] = arg.parse(input);
            }

            command.execute(parsed);

        } catch (IllegalArgumentException e) {
            StringBuilder argsFormatted = new StringBuilder();
            for (CommandArgument arg : expected) {
                argsFormatted.append("<{g}").append(arg.getName()).append("{s}> ");
            }
            String usageMessage = "Wrong input! Usage: {s}" + argsFormatted.toString().trim() + "{reset}.";
            CHAT_MANAGER.sendPersistent(CommandExecutor.class.getName(), CAT_FORMAT.format(usageMessage));

        } catch (Exception e) {
            LOGGER.error("Error executing command " + command.getName(), e);
        }
    }

    // Tokenize a command string into parts, respecting double quotes so tokens like "search list" are kept together
    private static String[] tokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
                continue; // don't include the quote character
            }
            if (Character.isWhitespace(c) && !inQuotes) {
                if (current.length() > 0) {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) tokens.add(current.toString());

        return tokens.toArray(new String[0]);
    }
}
