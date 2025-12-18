package me.kiriyaga.nami.feature.command;

public abstract class Command {
    protected final String name;
    protected final String[] aliases;
    protected final CommandArgument[] args;

    public Command(String name, CommandArgument[] args, String... aliases) {
        this.name = name;
        this.aliases = aliases;
        this.args = args;
    }

    public String getName() { return name; }

    public String[] getAliases() { return aliases; }

    public CommandArgument[] getArguments() { return args; }

    public boolean matches(String input) {
        String lower = input.toLowerCase();
        if (lower.equals(name)) return true;
        for (String alias : aliases) {
            if (lower.equals(alias.toLowerCase())) return true;
        }
        return false;
    }

    public abstract void execute(Object[] parsedArgs);
}
