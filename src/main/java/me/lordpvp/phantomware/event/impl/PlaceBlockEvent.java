package me.kiriyaga.nami.event.impl;

import me.kiriyaga.nami.event.Event;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class PlaceBlockEvent extends Event {
    private final ClientPlayerEntity player;
    private final Hand hand;
    private final BlockHitResult hitResult;


    public PlaceBlockEvent(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult) {
        this.player = player;
        this.hand = hand;
        this.hitResult = hitResult;
    }
    public ClientPlayerEntity getPlayer()
    {
        return player;
    }

    public Hand getHand()
    {
        return hand;
    }

    public BlockHitResult getHitResult()
    {
        return hitResult;
    }
}
