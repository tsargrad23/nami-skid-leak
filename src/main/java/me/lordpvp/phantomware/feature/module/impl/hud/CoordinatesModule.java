package me.kiriyaga.nami.feature.module.impl.hud;

import me.kiriyaga.nami.feature.module.HudElementModule;
import me.kiriyaga.nami.feature.module.RegisterModule;
import me.kiriyaga.nami.feature.setting.impl.BoolSetting;
import me.kiriyaga.nami.feature.setting.impl.EnumSetting;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

import static me.kiriyaga.nami.Nami.*;

@RegisterModule
public class CoordinatesModule extends HudElementModule {

    public enum LayoutMode {
        HORIZONTAL, VERTICAL
    }

    public final BoolSetting displayLabel = addSetting(new BoolSetting("Label", true));
    public final BoolSetting altCords = addSetting(new BoolSetting("AltCoordinates", true));
    public final EnumSetting<LayoutMode> layout = addSetting(new EnumSetting<>("Layout", LayoutMode.HORIZONTAL));

    public CoordinatesModule() {
        super("Coordinates", "Displays player coordinates.", 0, 0, 100, 30);
    }

    @Override
    public Text getDisplayText() {
        if (layout.get() == LayoutMode.VERTICAL) return null; // use getTextElements()

        if (MC.player == null || MC.world == null) {
            return CAT_FORMAT.format("{bg}XYZ: {bw}NaN");
        }

        double x = MC.player.getX();
        double y = MC.player.getY();
        double z = MC.player.getZ();

        boolean isNether = MC.world.getRegistryKey() == World.NETHER;
        boolean isOverworld = MC.world.getRegistryKey() == World.OVERWORLD;

        double xAlt = isNether ? x * 8 : x / 8;
        double zAlt = isNether ? z * 8 : z / 8;

        String formatted = "";

        if (displayLabel.get()) {
            formatted += "{bg}XYZ: ";
        }

        formatted += "{bw}" + formatNumber(x) + "{bg}, {bw}"
                + formatNumber(y) + "{bg}, {bw}"
                + formatNumber(z);

        if ((isOverworld || isNether) && altCords.get()) {
            formatted += " {bg}[{bw}" + formatNumber(xAlt) + "{bg}, {bw}" + formatNumber(zAlt) + "{bg}]";
        }

        width = FONT_MANAGER.getWidth(formatted.replaceAll("\\{.*?}", ""));
        height = FONT_MANAGER.getHeight();

        return CAT_FORMAT.format(formatted);
    }

    @Override
    public List<TextElement> getTextElements() {
        if (layout.get() != LayoutMode.VERTICAL) {
            return super.getTextElements(); // fallback to getDisplayText()
        }

        List<TextElement> lines = new ArrayList<>();

        if (MC.player == null || MC.world == null) {
            lines.add(new TextElement(CAT_FORMAT.format("{bg}XYZ: {bw}NaN"), 0, 0));
            return lines;
        }

        double x = MC.player.getX();
        double y = MC.player.getY();
        double z = MC.player.getZ();

        boolean isNether = MC.world.getRegistryKey() == World.NETHER;
        boolean isOverworld = MC.world.getRegistryKey() == World.OVERWORLD;

        double xAlt = isNether ? x * 8 : x / 8;
        double zAlt = isNether ? z * 8 : z / 8;

        int lineHeight = FONT_MANAGER.getHeight() + 1;
        int offsetY = 0;

        String fx = "{bg}X: {bw}" + formatNumber(x);
        if ((isOverworld || isNether) && altCords.get()) {
            fx += " {bg}[{bw}" + formatNumber(xAlt) + "{bg}]";
        }

        lines.add(new TextElement(CAT_FORMAT.format(fx), 0, offsetY));
        offsetY += lineHeight;

        lines.add(new TextElement(CAT_FORMAT.format("{bg}Y: {bw}" + formatNumber(y)), 0, offsetY));
        offsetY += lineHeight;

        String fz = "{bg}Z: {bw}" + formatNumber(z);
        if ((isOverworld || isNether) && altCords.get()) {
            fz += " {bg}[{bw}" + formatNumber(zAlt) + "{bg}]";
        }

        lines.add(new TextElement(CAT_FORMAT.format(fz), 0, offsetY));
        offsetY += lineHeight;

        int maxWidth = lines.stream()
                .mapToInt(te -> FONT_MANAGER.getWidth(te.text().getString().replaceAll("\\{.*?}", "")))
                .max().orElse(0);

        width = maxWidth;
        height = offsetY;

        return lines;
    }

    private String formatNumber(double val) {
        double rounded = Math.round(val * 10.0) / 10.0;
        return String.format("%.1f", rounded).replace(',', '.');
    }
}