package me.kiriyaga.nami.feature.command.impl;

import me.kiriyaga.nami.feature.command.Command;
import me.kiriyaga.nami.feature.command.CommandArgument;
import me.kiriyaga.nami.feature.command.RegisterCommand;
import net.minecraft.network.packet.s2c.common.DisconnectS2CPacket;
import net.minecraft.text.Text;

import static me.kiriyaga.nami.Nami.*;

@RegisterCommand
public class DisconnectCommand extends Command {

    public DisconnectCommand() {
        super(
                "disconnect",
                new CommandArgument[0],
                "dis", "discnect", "dissconnect", "logout"
        );
    }

    @Override
    public void execute(Object[] args) {
        if (MC.player != null && MC.getNetworkHandler() != null) {
            MC.getNetworkHandler().onDisconnect(new DisconnectS2CPacket(Text.empty()));
        }
    }
}
