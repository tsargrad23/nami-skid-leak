package me.kiriyaga.nami.feature.command;

import java.util.Arrays;
import java.util.Locale;

public abstract class CommandArgument {
    private final String name;
    private final boolean required;

    public CommandArgument(String name, boolean required) {
        this.name = name;
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public abstract Object parse(String input) throws IllegalArgumentException;

    public static class StringArg extends CommandArgument {
        private final int minLength, maxLength;

        public StringArg(String name, int minLength, int maxLength) {
            super(name, true);
            this.minLength = minLength;
            this.maxLength = maxLength;
        }

        public StringArg(String name) {
            this(name, 1, 256);
        }

        @Override
        public Object parse(String input) {
            if (input.length() < minLength || input.length() > maxLength) {
                throw new IllegalArgumentException("Argument '" + getName() + "' must be between " + minLength + " and " + maxLength + " characters.");
            }
            return input;
        }

        public Object parse(String[] input, int index) {
            return parse(input[index]);
        }
    }

    public static class IntArg extends CommandArgument {
        private final int min, max;

        public IntArg(String name, int min, int max) {
            super(name, true);
            this.min = min;
            this.max = max;
        }

        @Override
        public Object parse(String input) {
            try {
                int val = Integer.parseInt(input);
                if (val < min || val > max) {
                    throw new IllegalArgumentException("Argument '" + getName() + "' must be between " + min + " and " + max + ".");
                }
                return val;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Argument '" + getName() + "' must be an integer.");
            }
        }
    }

    public static class DoubleArg extends CommandArgument {
        private final double min, max;

        public DoubleArg(String name, double min, double max) {
            super(name, true);
            this.min = min;
            this.max = max;
        }

        @Override
        public Object parse(String input) {
            try {
                double val = Double.parseDouble(input);
                if (val < min || val > max) {
                    throw new IllegalArgumentException("Argument '" + getName() + "' must be between " + min + " and " + max + ".");
                }
                return val;
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Argument '" + getName() + "' must be a number.");
            }
        }
    }

    public static class ActionArg extends CommandArgument {
        private final String[] allowedValues;

        public ActionArg(String name, String... allowedValues) {
            super(name, true);
            this.allowedValues = Arrays.stream(allowedValues)
                    .map(s -> s.toLowerCase(Locale.ROOT))
                    .toArray(String[]::new);
        }

        public String[] getAllowedValues() {
            return allowedValues;
        }

        @Override
        public Object parse(String input) {
            return getCanonical(input);
        }

        public String getCanonical(String input) {
            String value = input.toLowerCase(Locale.ROOT);
            for (String allowed : allowedValues) {
                if (allowed.equals(value)) return allowed;
            }
            throw new IllegalArgumentException("Invalid value for argument '" + getName() + "'. Allowed: " + String.join(", ", allowedValues));
        }
    }

    public static class ModuleArg extends CommandArgument {
        public ModuleArg(String name) {
            super(name, true);
        }

        @Override
        public Object parse(String input) {
            return input;
        }
    }

    public static class SettingArg extends CommandArgument {
        public SettingArg(String name) {
            super(name, true);
        }

        @Override
        public Object parse(String input) {
            return input;
        }
    }

    public static class KeyBindArg extends CommandArgument {
        public KeyBindArg(String name) {
            super(name, true);
        }

        @Override
        public Object parse(String input) {
            return input;
        }
    }

    public static class ConfigNameArg extends CommandArgument {
        public ConfigNameArg(String name) {
            super(name, true);
        }

        @Override
        public Object parse(String input) {
            return input;
        }
    }

    public static class IdentifierArg extends CommandArgument {
        public enum Target { BLOCK, ITEM, SOUND, PARTICLE, ANY }

        private final Target target;

        public IdentifierArg(String name, Target target) {
            super(name, true);
            this.target = target;
        }

        public IdentifierArg(String name) {
            super(name, true);
            this.target = Target.ANY;
        }

        public Target getTarget() {
            return target;
        }

        @Override
        public Object parse(String input) {
            return input;
        }
    }

    public static class OnlinePlayerArg extends StringArg {
        public OnlinePlayerArg(String name) {
            super(name);
        }
    }

    public static class FriendArg extends StringArg {
        public FriendArg(String name) {
            super(name);
        }
    }

    public static class FriendNameArg extends StringArg {
        public FriendNameArg(String name, int minLength, int maxLength) {
            super(name, minLength, maxLength);
        }
    }
}