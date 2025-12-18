package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.PreTickEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.mixin.KeyBindingAccessor;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import static me.kiriyaga.nami.Nami.MC;

@RegisterModule
public class AutoWalkModule extends Module {

    public AutoWalkModule() {
        super("AutoWalk", "Automatically makes you walk.", ModuleCategory.of("Movement"),"autowalk");
    }

    @Override
    public void onDisable() {
        setWalkHeld(false);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onUpdateEvent(PreTickEvent event) {
        setWalkHeld(true);
    }

    private void setWalkHeld(boolean held) {
        KeyBinding walkKey = MC.options.forwardKey;
        InputUtil.Key boundKey = ((KeyBindingAccessor) walkKey).getBoundKey();
        int keyCode = boundKey.getCode();
        boolean physicallyPressed = InputUtil.isKeyPressed(MC.getWindow().getHandle(), keyCode);
        walkKey.setPressed(physicallyPressed || held);
    }
}
