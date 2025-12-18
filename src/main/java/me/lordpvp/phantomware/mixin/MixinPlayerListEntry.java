package me.kiriyaga.nami.mixin;

import me.kiriyaga.nami.feature.module.impl.client.cape.CapeModule;
import me.kiriyaga.nami.feature.module.impl.client.cape.CapeType;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.SkinTextures;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static me.kiriyaga.nami.Nami.MC;
import static me.kiriyaga.nami.Nami.MODULE_MANAGER;

@Mixin(PlayerListEntry.class)
public class MixinPlayerListEntry {

        @Inject(method = "getSkinTextures", at = @At("RETURN"), cancellable = true)
        private void getSkinTextures(CallbackInfoReturnable<SkinTextures> cir) {
            if (MC.player == null) return;

            PlayerListEntry self = (PlayerListEntry) (Object) this;

            if (!self.getProfile().getId().equals(MC.player.getUuid())) {
                return;
            }

            CapeModule capeModule = MODULE_MANAGER.getStorage().getByClass(CapeModule.class);

            if (capeModule != null && capeModule.isEnabled()) {
                CapeType type = capeModule.getSelectedCape();
                Identifier capeTex = type.getTexture();

                SkinTextures orig = cir.getReturnValue();

                SkinTextures custom = new SkinTextures(
                        orig.comp_1626(), // identifier
                        orig.comp_1911(), // url
                        capeTex, // cape
                        orig.comp_1628(), // ely
                        orig.comp_1629(), // body type
                        orig.comp_1630()  // idk
                );

                cir.setReturnValue(custom);
            }
        }
}
