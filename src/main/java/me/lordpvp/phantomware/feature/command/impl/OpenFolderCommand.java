package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;

import java.awt.Desktop;
import java.io.File;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class OpenFolderCommand extends Command {

    public OpenFolderCommand() {
        super("openfolder",
                new CommandArgument[] {},
                "of", "folder", "openf", "opendir");
    }

    @Override
    public void execute(Object[] args) {
        try {
            File dir = CONFIG_MANAGER.getDirectoryProvider().getBaseDir();
            if (!dir.exists() && !dir.mkdirs()) {
                CHAT_MANAGER.sendPersistent(OpenFolderCommand.class.getName(),
                        CAT_FORMAT.format("Failed to create folder: {g}" + dir.getAbsolutePath() + "{reset}."));
                return;
            }

            boolean opened = tryDesktopOpen(dir) || tryOsOpen(dir);
            if (opened) {
                CHAT_MANAGER.sendPersistent(OpenFolderCommand.class.getName(),
                        CAT_FORMAT.format("Opened folder: {g}" + dir.getAbsolutePath() + "{reset}."));
            } else {
                CHAT_MANAGER.sendPersistent(OpenFolderCommand.class.getName(),
                        CAT_FORMAT.format("Could not open folder automatically. Path: {g}" + dir.getAbsolutePath() + "{reset}."));
            }
        } catch (Exception e) {
            CHAT_MANAGER.sendPersistent(OpenFolderCommand.class.getName(),
                    CAT_FORMAT.format("Failed to open folder: {g}" + e.getMessage() + "{reset}."));
            LOGGER.error("Failed to open config folder", e);
        }
    }

    private boolean tryDesktopOpen(File dir) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(dir);
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    private boolean tryOsOpen(File dir) {
        String path = dir.getAbsolutePath();
        String os = System.getProperty("os.name").toLowerCase();
        try {
            if (os.contains("win")) {
                new ProcessBuilder("explorer", path).start();
                return true;
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", path).start();
                return true;
            } else {
                new ProcessBuilder("xdg-open", path).start();
                return true;
            }
        } catch (Throwable ignored) {}
        return false;
    }
}
