package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import me.kiriyaga.nami.feature.setting.impl.WhitelistSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.*;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class EntityListModule extends HudElementModule {

    public enum SortMode {
        alphabetical,
        descending,
        ascending
    }

    private final List<TextElement> elements = new ArrayList<>();
    private long lastUpdateTime = 0;
    private final long updateIntervalMs = 3000;

    private final WhitelistSetting whitelist = addSetting(new WhitelistSetting("Whitelist", false, WhitelistSetting.Type.ENTITY));
    public final EnumSetting<SortMode> sortMode = addSetting(new EnumSetting<>("Sort", SortMode.descending));
    public final BoolSetting onlyLiving = addSetting(new BoolSetting("OnlyLiving", false));

    public EntityListModule() {
        super("EntityList", "Shows nearby entities", 0, 0, 50, 10);
    }

    @Override
    public List<TextElement> getTextElements() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastUpdateTime < updateIntervalMs) {
            return elements;
        }
        lastUpdateTime = currentTime;

        elements.clear();
        Map<String, Integer> entityCounts = new HashMap<>();

        for (Entity entity : ENTITY_MANAGER.getAllEntities()) {
            if (entity == MC.player) continue;
            if (onlyLiving.get() && !(entity instanceof LivingEntity)) continue;

            if (whitelist.get()) {
                Identifier entityId = Registries.ENTITY_TYPE.getId(entity.getType());
                if (entityId == null || !whitelist.isWhitelisted(entityId)) {
                    continue;
                }
            }

            String name = entity.getName().getString();
            entityCounts.put(name, entityCounts.getOrDefault(name, 0) + 1);
        }

        List<String> sortedNames = new ArrayList<>(entityCounts.keySet());

        switch (sortMode.get()) {
            case alphabetical -> sortedNames.sort(String::compareToIgnoreCase);
            case descending -> sortedNames.sort((a, b) -> Integer.compare(
                    getTextWidth(b, entityCounts.get(b)),
                    getTextWidth(a, entityCounts.get(a))
            ));
            case ascending -> sortedNames.sort((a, b) -> Integer.compare(
                    getTextWidth(a, entityCounts.get(a)),
                    getTextWidth(b, entityCounts.get(b))
            ));
        }

        int yOffset = 0;
        int maxWidth = 0;

        for (String name : sortedNames) {
            int count = entityCounts.get(name);
            Text text = CAT_FORMAT.format("{bg}" + name + (count > 1 ? " {bw}(x" + count + ")" : ""));
            int textWidth = FONT_MANAGER.getWidth(text);
            elements.add(new TextElement(text, 0, yOffset));

            maxWidth = Math.max(maxWidth, textWidth);
            yOffset += FONT_MANAGER.getHeight();
        }

        this.width = maxWidth;
        this.height = yOffset;

        return elements;
    }

    private int getTextWidth(String name, int count) {
        Text text = CAT_FORMAT.format("{bg}" + name + (count > 1 ? " {bw}(x" + count + ")" : ""));
        return FONT_MANAGER.getWidth(text);
    }
}