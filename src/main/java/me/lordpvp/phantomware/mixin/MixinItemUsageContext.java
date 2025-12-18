package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.event.impl.ItemEvent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.EVENT_MANAGER;
import static me.kiriyaga.nami.Nami.MC;

@Mixin(ItemUsageContext.class)
public final class MixinItemUsageContext {
    @Inject(method = "getStack", at = @At("RETURN"), cancellable = true)
    public void getStack(final CallbackInfoReturnable<ItemStack> info) {
        if (MC.player == null)
            return;

        ItemEvent event = new ItemEvent();
        EVENT_MANAGER.post(event);

        if (info.getReturnValue().equals(MC.player.getMainHandStack()) && event.isCancelled())
            info.setReturnValue(event.getStack());
    }
}