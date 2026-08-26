package martydevs.marty.helper.map;

import net.minecraft.util.ARGB;
import net.minecraft.world.level.material.MapColor;
import java.lang.reflect.Field;

public final class MapColorHelper {

    public static final MapColor[] MATERIAL_COLORS;
    private static final AdditionalMapColorData[] ADDITIONAL_MAP_COLOR_DATA;

    static {
        try {
            Field field = MapColor.class.getDeclaredField("MATERIAL_COLORS");
            field.setAccessible(true);
            MATERIAL_COLORS = (MapColor[]) field.get(null);
        }
        catch (Throwable throwable) {
            throw new ExceptionInInitializerError(throwable);
        }

        ADDITIONAL_MAP_COLOR_DATA = new AdditionalMapColorData[MATERIAL_COLORS.length];
        for (int i = 0; i < MATERIAL_COLORS.length; i++) {
            MapColor mapColor = MATERIAL_COLORS[i];
            if(mapColor == null) break;

            int rgbValue = mapColor.col & 0x00FFFFFF;

            PaletteEntry[] paletteEntries = new PaletteEntry[3];
            for (int j = 0; j < paletteEntries.length; j++) {
                MapColor.Brightness brightness = MapColor.Brightness.byId(j);
                paletteEntries[j] = PaletteEntry.of(
                        mapColor,
                        brightness,
                        ARGB.scaleRGB(rgbValue, brightness.modifier)
                );
            }

            ADDITIONAL_MAP_COLOR_DATA[i] = new AdditionalMapColorData(paletteEntries);
        }
    }

    private MapColorHelper() {}

    public static PaletteEntry[] paletteEntries(MapColor color) {
        return ADDITIONAL_MAP_COLOR_DATA[color.id].values;
    }

    private record AdditionalMapColorData(PaletteEntry[] values) { }

}
