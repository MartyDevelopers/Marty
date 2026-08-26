package martydevs.marty.helper.map;

import martydevs.marty.model.image.BetterColor;
import martydevs.marty.util.ColorUtil;
import net.minecraft.world.level.material.MapColor;

public record PaletteEntry(
        MapColor mapColor, MapColor.Brightness brightness,
        int rgb, // RGB representation
        int redComponent, int greenComponent, int blueComponent, // RGB representation by components
        float[][] labRepresentations
) {

    public static PaletteEntry of(MapColor mapColor, MapColor.Brightness brightness, int rgb) {
        int r = ColorUtil.redComponent(rgb);
        int g = ColorUtil.greenComponent(rgb);
        int b = ColorUtil.blueComponent(rgb);

        var values = BetterColor.Tristimulus.values();
        float[][] labRepresentations = new float[values.length][3];
        for (BetterColor.Tristimulus tristimulus : values) {
            labRepresentations[tristimulus.ordinal()] = ColorUtil.rgbToLab(r, g, b, tristimulus.xyz);
        }

        return new PaletteEntry(
                mapColor, brightness,
                rgb,
                r, g, b,
                labRepresentations
        );
    }

    public int waterColorIndex() {
        if(mapColor == MapColor.WATER) return -1;
        return brightness.id;
    }

}
