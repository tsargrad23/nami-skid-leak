package me.kiriyaga.nami.feature.module.impl.movement;

import me.kiriyaga.nami.event.EventPriority;
import me.kiriyaga.nami.event.SubscribeEvent;
import me.kiriyaga.nami.event.impl.SprintResetEvent;
import me.kiriyaga.nami.feature.module.ModuleCategory;
import me.kiriyaga.nami.feature.module.Module;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.IntSetting;

@RegisterModule
public class SprintResetModule extends Module {

    private final IntSetting chance = addSetting(new IntSetting("Chance", 100, 0, 100));

    public SprintResetModule() {
        super("SprintReset", "Control how sprint resets after attacking.", ModuleCategory.of("Movement"), "sprintreset");
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onSprintResetEvent(SprintResetEvent event) {
        if (!event.isCancelled()) {
            this.setDisplayInfo(chance.get().toString());

            int random = (int) (Math.random() * 100);
            if (random < chance.get())
                event.cancel();
        }
    }
}
